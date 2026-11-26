-- Saga Detective - V3: permissoes e usuarios de acesso

CREATE TABLE permissao (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    descricao VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permissao_descricao (descricao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario_permissao (
    usuario_id   BIGINT NOT NULL,
    permissao_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, permissao_id),
    CONSTRAINT fk_up_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario (id)   ON DELETE CASCADE,
    CONSTRAINT fk_up_permissao FOREIGN KEY (permissao_id) REFERENCES permissao (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO permissao (descricao) VALUES
('ROLE_CADASTRAR_CASO'),
('ROLE_PESQUISAR_CASO'),
('ROLE_REMOVER_CASO'),
('ROLE_CADASTRAR_SUSPEITO'),
('ROLE_PESQUISAR_SUSPEITO'),
('ROLE_REMOVER_SUSPEITO'),
('ROLE_INICIAR_SESSAO'),
('ROLE_PESQUISAR_SESSAO');

-- Cole abaixo os hashes gerados pelo GeradorDeSenha (60 caracteres, entre aspas simples)
INSERT INTO usuario (nome, email, senha, perfil) VALUES
('Administrador',    'admin@saga.test',   '$2a$10$g3CNAy862Od0iGbs2RylguLtaADuLGCCwqPKwj1ydgEzaoPKtczVK',   'ROLE_ADMIN'),
('Jogador de Teste', 'jogador@saga.test', '$2a$10$dJPjnIIaKJLW6I6vR1WYmeBULyiJQMzDJDnmsywen/WiNJMtsuVQq', 'ROLE_JOGADOR');

-- Administrador: todas as permissoes
INSERT INTO usuario_permissao (usuario_id, permissao_id)
SELECT u.id, p.id
FROM usuario u CROSS JOIN permissao p
WHERE u.email = 'admin@saga.test';

-- Jogador: so consulta casos e suspeitos, e inicia e consulta sessoes
INSERT INTO usuario_permissao (usuario_id, permissao_id)
SELECT u.id, p.id
FROM usuario u
JOIN permissao p ON p.descricao IN (
    'ROLE_PESQUISAR_CASO',
    'ROLE_PESQUISAR_SUSPEITO',
    'ROLE_INICIAR_SESSAO',
    'ROLE_PESQUISAR_SESSAO')
WHERE u.email = 'jogador@saga.test';