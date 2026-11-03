-- =====================================================
-- Saga Detective - V2: caso de teste
-- =====================================================

INSERT INTO caso (id, titulo, descricao_crime, dificuldade, limite_horas) VALUES
(1, 'O Crime na Mansão Whitmore',
 'Lorde Edmund Whitmore foi encontrado morto em seu escritório na noite de 12 de março. A porta estava trancada por dentro e uma taça de vinho jazia quebrada no chão.',
 'FACIL', 10);

INSERT INTO suspeito (id, caso_id, nome, ocupacao, biografia, relacao_vitima) VALUES
(1, 1, 'Margaret Whitmore', 'Esposa', 'Casada com Edmund há 20 anos. Herdeira de metade da fortuna.', 'Esposa da vítima'),
(2, 1, 'James Hartley', 'Mordomo', 'Serve a família há 30 anos e conhece todos os segredos da casa.', 'Funcionário de confiança'),
(3, 1, 'Dr. Victor Crane', 'Médico da família', 'Cuidava da saúde de Edmund e tinha dívidas de jogo.', 'Médico e amigo da vítima');

INSERT INTO arma (id, caso_id, nome) VALUES
(1, 1, 'Veneno no vinho'),
(2, 1, 'Candelabro'),
(3, 1, 'Abridor de cartas');

INSERT INTO motivacao (id, caso_id, descricao) VALUES
(1, 1, 'Herança'),
(2, 1, 'Chantagem'),
(3, 1, 'Vingança');

INSERT INTO solucao_caso (caso_id, suspeito_id, arma_id, motivacao_id) VALUES
(1, 3, 1, 2);

INSERT INTO pista (id, caso_id, nome, descricao, nivel_revelacao, custo_horas) VALUES
(1, 1, 'Taça quebrada', 'Resíduos de um sedativo foram encontrados na taça.', 1, 1),
(2, 1, 'Carta de dívidas', 'Uma carta mostra que alguém devia dinheiro a Edmund.', 2, 2),
(3, 1, 'Receituário rasgado', 'Fragmento de receituário com a assinatura do Dr. Crane.', 3, 2);

INSERT INTO depoimento (id, suspeito_id, topico, texto_resposta, pista_desbloqueada_id, custo_horas) VALUES
(1, 1, 'Álibi', 'Eu estava no jardim, ao telefone com minha irmã.', NULL, 1),
(2, 2, 'Taça de vinho', 'Servi o vinho às 21h, mas o Dr. Crane pediu para levar a taça ao lorde.', 1, 1),
(3, 3, 'Dívidas', 'Não sei do que fala... Edmund e eu éramos amigos.', 2, 1),
(4, 3, 'Receitas médicas', 'Ah, aquele papel? Deve ter sido esquecido por engano.', 3, 2);
