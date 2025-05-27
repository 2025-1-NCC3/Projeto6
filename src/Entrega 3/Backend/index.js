const express = require("express");
const cors = require("cors");
const { MongoClient } = require("mongodb");
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt"); // Importa bcrypt

// Função Cifra de César (mantida do código original)
function cifraDeCesar(texto, deslocamento) {
  // Garante que o texto seja uma string
  if (typeof texto !== 'string') {
    texto = String(texto);
  }
  return texto
    .split('')
    .map(char => {
      const codigo = char.charCodeAt(0);
      // Verifica se é letra maiúscula (A-Z)
      if (codigo >= 65 && codigo <= 90) {
        return String.fromCharCode(((codigo - 65 + deslocamento) % 26) + 65);
      // Verifica se é letra minúscula (a-z)
      } else if (codigo >= 97 && codigo <= 122) {
        return String.fromCharCode(((codigo - 97 + deslocamento) % 26) + 97);
      // Mantém outros caracteres como estão
      } else {
        return char;
      }
    })
    .join('');
}

const app = express();
const port = process.env.PORT || 3000;
const saltRounds = 10; // Define o custo do hashing para bcrypt

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// URI de conexão do MongoDB Atlas fornecida pelo usuário
const uri = "mongodb+srv://gg40790:WHyP9fWvXtxl9L4x@ubertracker.ij4j6gm.mongodb.net/";
const client = new MongoClient(uri);

let db; // Variável para armazenar a instância do banco de dados
const dbName = "ubertracker"; // Nome do banco de dados

// Função assíncrona para conectar ao MongoDB
async function connectDB() {
  try {
    await client.connect();
    db = client.db(dbName);
    console.log(`Conectado ao MongoDB Atlas, banco de dados: ${dbName}`);
    // Criar índices únicos para Email em ambas as coleções (se não existirem)
    // Isso previne emails duplicados e otimiza a busca no login
    // A criptografia do email complica a criação de índice único direto, mas podemos indexar o campo.
    await db.collection("passageiros").createIndex({ Email: 1 });
    await db.collection("motoristas").createIndex({ Email: 1 });
    console.log("Índices para Email criados/verificados.");
  } catch (err) {
    console.error("Erro ao conectar ou criar índices no MongoDB:", err);
    process.exit(1); // Encerra a aplicação se a conexão/setup falhar
  }
}

connectDB(); // Chama a função para conectar ao iniciar a aplicação

// Rota principal
app.get("/", (req, res) => {
  res.send("Olá do servidor MongoDB com Bcrypt!");
});

// Rota para buscar todos os Passageiros
app.get("/Passageiro", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  try {
    const passageirosCollection = db.collection("passageiros");
    const passageiros = await passageirosCollection.find({}, { projection: { Senha: 0 } }).toArray(); // Exclui a senha do resultado
    res.json(passageiros);
  } catch (err) {
    console.error("Erro ao buscar passageiros:", err);
    res.status(500).json({ error: "Erro interno ao buscar passageiros" });
  }
});

// Rota para buscar todos os Motoristas
app.get("/Motorista", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  try {
    const motoristasCollection = db.collection("motoristas");
    const motoristas = await motoristasCollection.find({}, { projection: { Senha: 0 } }).toArray(); // Exclui a senha do resultado
    res.json(motoristas);
  } catch (err) {
    console.error("Erro ao buscar motoristas:", err);
    res.status(500).json({ error: "Erro interno ao buscar motoristas" });
  }
});

// Rota para CADASTRAR um novo Passageiro (com bcrypt)
app.post("/regp", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Nome, Senha, CPF, Email } = req.body;

  if (!Nome || !Senha || !CPF || !Email) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios!" });
  }

  try {
    // Criptografa dados sensíveis (exceto senha, que será hasheada)
    const deslocamento = 3;
    const NomeCrip = cifraDeCesar(Nome, deslocamento);
    const CPFcrip = cifraDeCesar(CPF, deslocamento);
    const EmailCrip = cifraDeCesar(Email, deslocamento);

    // Gera o hash da senha
    const hashedSenha = await bcrypt.hash(Senha, saltRounds);

    const novoPassageiro = {
      Nome: NomeCrip,
      Senha: hashedSenha, // Salva o HASH da senha
      CPF: CPFcrip,
      Email: EmailCrip, // Salva o Email criptografado
      DataCadastro: new Date()
    };

    const passageirosCollection = db.collection("passageiros");

    // Verifica se o email já existe (usando a versão criptografada)
    const existingUser = await passageirosCollection.findOne({ Email: EmailCrip });
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
    // Tratamento genérico para outros erros
    res.status(500).json({ error: "Erro interno ao inserir passageiro" });
  }
});

// Rota para CADASTRAR um novo Motorista (com bcrypt)
app.post("/regm", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo, Celular } = req.body;

  if (!Nome || !Senha || !CPF || !Email || !CNH || !Placa || !Marca || !Modelo || !Celular) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios!" });
  }

  try {
    // Criptografa dados sensíveis
    const deslocamento = 3;
    const NomeCrip = cifraDeCesar(Nome, deslocamento);
    const EmailCrip = cifraDeCesar(Email, deslocamento);
    const CPFcrip = cifraDeCesar(CPF, deslocamento);
    const CelularCrip = cifraDeCesar(Celular, deslocamento);

    // Gera o hash da senha
    const hashedSenha = await bcrypt.hash(Senha, saltRounds);

    const novoMotorista = {
      Nome: NomeCrip,
      Senha: hashedSenha, // Salva o HASH da senha
      Email: EmailCrip, // Salva o Email criptografado
      CPF: CPFcrip,
      CNH, // Mantido como texto plano conforme original
      Placa, // Mantido como texto plano
      Marca, // Mantido como texto plano
      Modelo, // Mantido como texto plano
      Celular: CelularCrip,
      DataCadastro: new Date()
    };

    const motoristasCollection = db.collection("motoristas");

    // Verifica se o email já existe (usando a versão criptografada)
    const existingUser = await motoristasCollection.findOne({ Email: EmailCrip });
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

// Rota para LOGIN de Passageiro (com bcrypt)
app.post("/loginp", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Email, Senha } = req.body;

  if (!Email || !Senha) {
    return res.status(400).json({ error: "Email e Senha são obrigatórios!" });
  }

  try {
    const passageirosCollection = db.collection("passageiros");
    // Criptografa o email fornecido para buscar no banco
    const deslocamento = 3;
    const EmailCrip = cifraDeCesar(Email, deslocamento);

    // Busca o passageiro pelo email criptografado
    const passageiro = await passageirosCollection.findOne({ Email: EmailCrip });

    if (!passageiro) {
      return res.status(401).json({ error: "Credenciais inválidas" }); // Usuário não encontrado
    }

    // Compara a senha fornecida com o hash armazenado
    const match = await bcrypt.compare(Senha, passageiro.Senha);

    if (match) {
      // Login bem-sucedido
      // NÃO retorne a senha hasheada!
      // Poderia retornar dados do usuário ou um token JWT aqui
      res.json({ message: "Login de passageiro bem-sucedido!", userId: passageiro._id });
    } else {
      // Senha incorreta
      res.status(401).json({ error: "Credenciais inválidas" });
    }
  } catch (err) {
    console.error("Erro no login do passageiro:", err);
    res.status(500).json({ error: "Erro interno no login" });
  }
});

// Rota para LOGIN de Motorista (com bcrypt)
app.post("/loginm", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });
  const { Email, Senha } = req.body;

  if (!Email || !Senha) {
    return res.status(400).json({ error: "Email e Senha são obrigatórios!" });
  }

  try {
    const motoristasCollection = db.collection("motoristas");
    // Criptografa o email fornecido para buscar no banco
    const deslocamento = 3;
    const EmailCrip = cifraDeCesar(Email, deslocamento);

    // Busca o motorista pelo email criptografado
    const motorista = await motoristasCollection.findOne({ Email: EmailCrip });

    if (!motorista) {
      return res.status(401).json({ error: "Credenciais inválidas" }); // Usuário não encontrado
    }

    // Compara a senha fornecida com o hash armazenado
    const match = await bcrypt.compare(Senha, motorista.Senha);

    if (match) {
      // Login bem-sucedido
      res.json({ message: "Login de motorista bem-sucedido!", userId: motorista._id });
    } else {
      // Senha incorreta
      res.status(401).json({ error: "Credenciais inválidas" });
    }
  } catch (err) {
    console.error("Erro no login do motorista:", err);
    res.status(500).json({ error: "Erro interno no login" });
  }
});


/* Caminho da Corrida Iniciada

// Rota para registrar uma nova corrida
app.post("/corrida", async (req, res) => {
  if (!db) return res.status(503).json({ error: "Banco de dados não conectado." });

  const { passageiroId, motoristaId, origem, destino, preco, horarioInicio, horarioFim } = req.body;

  if (!passageiroId || !motoristaId || !origem || !destino || !preco || !horarioInicio || !horarioFim) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios." });
  }

  try {
    const novaCorrida = {
      passageiroId,
      motoristaId,
      origem,
      destino,
      preco,
      horarioInicio: new Date(horarioInicio),
      horarioFim: new Date(horarioFim),
      dataRegistro: new Date()
    };

    const result = await db.collection("corridas").insertOne(novaCorrida);

    res.status(201).json({
      message: "Corrida registrada com sucesso!",
      id: result.insertedId,
    });
  } catch (err) {
    console.error("Erro ao registrar corrida:", err);
    res.status(500).json({ error: "Erro interno ao registrar corrida" });
  }
});


*/ 


// Inicia o servidor na porta definida
const server = app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});

// Tratamento para encerramento gracioso
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

