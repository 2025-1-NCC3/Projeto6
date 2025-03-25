var express = require("express");
var cors = require("cors");
var sqlite3 = require("sqlite3").verbose();
var bodyParser = require("body-parser");

var app = express();
var port = process.env.PORT || 3000;

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Caminho para o banco de dados SQLite
var CAMINHO_DB = "BD.db";
//Conexão com o BD
var db = new sqlite3.Database(CAMINHO_DB, (err) => {
  if (err) {
    console.error("Erro ao conectar ao banco:", err.message);
  } else {
    console.log("Banco de dados conectado!");
  }
});

//Rota para acessaar o BD dos passageiros
app.get("/Passageiro", function (req, res) {
  db.all("SELECT * FROM Passageiro", [], (err, rows) => {
    if (err) {
      console.error("Erro ao buscar dados:", err);
      return res.status(500).json({ error: "Erro ao buscar dados" });
    }
    res.json(rows);
  });
});

app.get("/Motorista", function (req, res) {
  db.all("SELECT * FROM Motorista", [], (err, rows) => {
    if (err) {
      console.error("Erro ao buscar dados:", err);
      return res.status(500).json({ error: "Erro ao buscar dados" });
    }
    res.json(rows);
  });
});

//Rota Main
app.get("/", function (req, res) {
  res.send("Olá do servidor!");
});

//Rota para cadastrar um novo Passageiro
app.post("/regp", function (req, res) {
  var { Nome, Senha, CPF, Email } = req.body;

  if (!Nome || !Senha || !CPF || !Email) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios!" });
  }

  var sql =
    "INSERT INTO Passageiro (Nome, Senha, CPF, Email) VALUES (?, ?, ?, ?)";

  db.run(sql, [Nome, Senha, CPF, Email], function (err) {
    if (err) {
      console.error("Erro ao inserir passageiro:", err);
      return res.status(500).json({ error: "Erro ao inserir passageiro" });
    }
    res.json({
      message: "Passageiro cadastrado com sucesso!",
      id: this.lastID,
    });
  });
});

//Rota para cadastrar um novo Passageiro
app.post("/regm", function (req, res) {
  var { Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo, Celular } =
    req.body;

  if (
    !Nome ||
    !Senha ||
    !CPF ||
    !Email ||
    !CPF ||
    !CNH ||
    !Placa ||
    !Marca ||
    !Modelo ||
    !Celular
  ) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios!" });
  }

  var sql =
    "INSERT INTO Motorista (Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo,Celular ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  db.run(
    sql,
    [Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo, Celular],
    function (err) {
      if (err) {
        console.error("Erro ao inserir Motorista:", err);
        return res.status(500).json({ error: "Erro ao inserir Motorista" });
      }
      res.json({
        message: "Motorista cadastrado com sucesso!",
        id: this.lastID,
      });
    }
  );
});

// inicia o servidor na porta definida
app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});
