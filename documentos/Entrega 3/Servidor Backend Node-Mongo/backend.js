const express = require("express");
const cors = require("cors");
const { MongoClient } = require("mongodb");
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt");

const app = express();
const port = process.env.PORT || 3000;
const saltRounds = 10;

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

const uri = "mongodb+srv://gg40790:WHyP9fWvXtxl9L4x@ubertracker.ij4j6gm.mongodb.net/";
const client = new MongoClient(uri);
let db;
const dbName = "ubertracker";

async function connectDB() {
  try {
    await client.connect();
    db = client.db(dbName);
    console.log(`Conectado ao MongoDB Atlas, banco de dados: ${dbName}`);
    await db.collection("passageiros").createIndex({ EmailCriptografado: 1 }, { unique: true });
    await db.collection("motoristas").createIndex({ EmailCriptografado: 1 }, { unique: true });
    console.log("Índices para EmailCriptografado criados/verificados.");
  } catch (err) {
    console.error("Erro ao conectar ou criar índices no MongoDB:", err);
    process.exit(1);
  }
}

connectDB();

app.get("/", (req, res) => {
  res.send("Olá do servidor MongoDB com Bcrypt - Backend Modificado!");
});

// Rota para CADASTRAR um novo Passageiro
app.post("/regp", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Nome, Sobrenome, Senha, CPF, Email, DataNascimento, Telefone, Endereco } = req.body;

  if (!Nome || !Sobrenome || !Senha || !CPF || !Email || !DataNascimento || !Telefone || !Endereco) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios para cadastro de passageiro!" });
  }

  try {
    const hashedSenha = await bcrypt.hash(Senha, saltRounds);

    const novoPassageiro = {
      NomeCriptografado: Nome,
      SobrenomeCriptografado: Sobrenome,
      SenhaHash: hashedSenha,
      CPFCriptografado: CPF,
      EmailCriptografado: Email,
      DataNascimentoCriptografado: DataNascimento,
      TelefoneCriptografado: Telefone,
      EnderecoCriptografado: Endereco,
      DataCadastro: new Date()
    };

    const passageirosCollection = db.collection("passageiros");
    const existingUser = await passageirosCollection.findOne({ EmailCriptografado: Email });
    if (existingUser) {
      return res.status(409).json({ error: "Email já cadastrado." });
    }

    const result = await passageirosCollection.insertOne(novoPassageiro);
    res.status(201).json({
      message: "Passageiro cadastrado com sucesso!",
      id: result.insertedId,
    });
  } catch (err) {
    console.error("Erro ao inserir passageiro:", err);
    res.status(500).json({ error: "Erro interno ao inserir passageiro" });
  }
});

// Rota para CADASTRAR um novo Motorista
app.post("/regm", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Nome, Sobrenome, Senha, Email, CPF, DataNascimento, Telefone, Endereco, CNH, Placa, Marca, Modelo, AnoVeiculo, CategoriaCNH } = req.body;

  if (!Nome || !Sobrenome || !Senha || !Email || !CPF || !DataNascimento || !Telefone || !Endereco || !CNH || !Placa || !Marca || !Modelo || !AnoVeiculo || !CategoriaCNH) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios para cadastro de motorista!" });
  }

  try {
    const hashedSenha = await bcrypt.hash(Senha, saltRounds);

    const novoMotorista = {
      NomeCriptografado: Nome,
      SobrenomeCriptografado: Sobrenome,
      SenhaHash: hashedSenha,
      EmailCriptografado: Email,
      CPFCriptografado: CPF,
      DataNascimentoCriptografado: DataNascimento,
      TelefoneCriptografado: Telefone,
      EnderecoCriptografado: Endereco,
      CNH: CNH,
      Placa: Placa,
      Marca: Marca,
      Modelo: Modelo,
      AnoVeiculo: AnoVeiculo,
      CategoriaCNH: CategoriaCNH,
      DataCadastro: new Date()
    };

    const motoristasCollection = db.collection("motoristas");
    const existingUser = await motoristasCollection.findOne({ EmailCriptografado: Email });
    if (existingUser) {
      return res.status(409).json({ error: "Email já cadastrado." });
    }

    const result = await motoristasCollection.insertOne(novoMotorista);
    res.status(201).json({
      message: "Motorista cadastrado com sucesso!",
      id: result.insertedId,
    });
  } catch (err) {
    console.error("Erro ao inserir Motorista:", err);
    res.status(500).json({ error: "Erro interno ao inserir Motorista" });
  }
});

// Rota para LOGIN de Passageiro
app.post("/loginp", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Email, Senha } = req.body;

  if (!Email || !Senha) {
    return res.status(400).json({ error: "Email e Senha são obrigatórios!" });
  }

  try {
    const passageirosCollection = db.collection("passageiros");
    const passageiro = await passageirosCollection.findOne({ EmailCriptografado: Email });

    if (!passageiro) {
      return res.status(401).json({ error: "Credenciais inválidas" });
    }

    const match = await bcrypt.compare(Senha, passageiro.SenhaHash);

    if (match) {
      res.json({ message: "Login de passageiro bem-sucedido!", userId: passageiro._id, nome: passageiro.NomeCriptografado });
    } else {
      res.status(401).json({ error: "Credenciais inválidas" });
    }
  } catch (err) {
    console.error("Erro no login do passageiro:", err);
    res.status(500).json({ error: "Erro interno no login" });
  }
});

// Rota para LOGIN de Motorista
app.post("/loginm", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Email, Senha } = req.body;

  if (!Email || !Senha) {
    return res.status(400).json({ error: "Email e Senha são obrigatórios!" });
  }

  try {
    const motoristasCollection = db.collection("motoristas");
    const motorista = await motoristasCollection.findOne({ EmailCriptografado: Email });

    if (!motorista) {
      return res.status(401).json({ error: "Credenciais inválidas" });
    }

    const match = await bcrypt.compare(Senha, motorista.SenhaHash);

    if (match) {
      res.json({ message: "Login de motorista bem-sucedido!", userId: motorista._id, nome: motorista.NomeCriptografado });
    } else {
      res.status(401).json({ error: "Credenciais inválidas" });
    }
  } catch (err) {
    console.error("Erro no login do motorista:", err);
    res.status(500).json({ error: "Erro interno no login" });
  }
});

const server = app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});

process.on('SIGINT', async () => {
  console.log('\nRecebido SIGINT. Fechando servidor e conexão com MongoDB...');
  server.close(async () => {
    console.log('Servidor HTTP fechado.');
    await client.close();
    console.log('Conexão com MongoDB fechada.');
    process.exit(0);
  });
});

process.on('SIGTERM', async () => {
  console.log('\nRecebido SIGTERM. Fechando servidor e conexão com MongoDB...');
  server.close(async () => {
    console.log('Servidor HTTP fechado.');
    await client.close();
    console.log('Conexão com MongoDB fechada.');
    process.exit(0);
  });
});
