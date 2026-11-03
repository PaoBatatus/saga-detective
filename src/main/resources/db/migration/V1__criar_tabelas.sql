-- =====================================================
-- Saga Detective - V1: criação da estrutura do banco
-- MySQL 8+ | InnoDB | utf8mb4
-- =====================================================

CREATE TABLE usuario (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    nome             VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    senha            VARCHAR(255) NOT NULL,              -- hash BCrypt
    pontuacao_geral  INT          NOT NULL DEFAULT 0,
    perfil           VARCHAR(20)  NOT NULL DEFAULT 'ROLE_JOGADOR',
    avatar_url       VARCHAR(255) NULL,
    data_cadastro    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_email (email),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ROLE_JOGADOR','ROLE_ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE caso (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    titulo            VARCHAR(150) NOT NULL,
    descricao_crime   TEXT         NOT NULL,
    dificuldade       VARCHAR(10)  NOT NULL,
    limite_horas      INT          NOT NULL,
    imagem_capa       VARCHAR(255) NULL,
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT ck_caso_dificuldade CHECK (dificuldade IN ('FACIL','MEDIO','DIFICIL')),
    CONSTRAINT ck_caso_limite CHECK (limite_horas > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE suspeito (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    caso_id          BIGINT       NOT NULL,
    nome             VARCHAR(100) NOT NULL,
    ocupacao         VARCHAR(100) NULL,
    biografia        TEXT         NULL,
    relacao_vitima   VARCHAR(255) NULL,
    imagem_url       VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_suspeito_caso FOREIGN KEY (caso_id) REFERENCES caso (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE arma (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    caso_id   BIGINT       NOT NULL,
    nome      VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_arma_caso FOREIGN KEY (caso_id) REFERENCES caso (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE motivacao (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    caso_id    BIGINT       NOT NULL,
    descricao  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_motivacao_caso FOREIGN KEY (caso_id) REFERENCES caso (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Gabarito do caso (culpado + arma + motivação corretos)
CREATE TABLE solucao_caso (
    caso_id       BIGINT NOT NULL,
    suspeito_id   BIGINT NOT NULL,
    arma_id       BIGINT NOT NULL,
    motivacao_id  BIGINT NOT NULL,
    PRIMARY KEY (caso_id),
    CONSTRAINT fk_solucao_caso      FOREIGN KEY (caso_id)      REFERENCES caso (id) ON DELETE CASCADE,
    CONSTRAINT fk_solucao_suspeito  FOREIGN KEY (suspeito_id)  REFERENCES suspeito (id),
    CONSTRAINT fk_solucao_arma      FOREIGN KEY (arma_id)      REFERENCES arma (id),
    CONSTRAINT fk_solucao_motivacao FOREIGN KEY (motivacao_id) REFERENCES motivacao (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pista (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    caso_id           BIGINT       NOT NULL,
    nome              VARCHAR(100) NOT NULL,
    descricao         TEXT         NOT NULL,
    nivel_revelacao   INT          NOT NULL DEFAULT 1,
    custo_horas       INT          NOT NULL DEFAULT 0,   -- custo para buscar a pista diretamente
    PRIMARY KEY (id),
    CONSTRAINT fk_pista_caso FOREIGN KEY (caso_id) REFERENCES caso (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE depoimento (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    suspeito_id             BIGINT       NOT NULL,
    topico                  VARCHAR(150) NOT NULL,
    texto_resposta          TEXT         NOT NULL,
    pista_desbloqueada_id   BIGINT       NULL,
    custo_horas             INT          NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_depoimento_suspeito FOREIGN KEY (suspeito_id) REFERENCES suspeito (id) ON DELETE CASCADE,
    CONSTRAINT fk_depoimento_pista    FOREIGN KEY (pista_desbloqueada_id) REFERENCES pista (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sessao_investigacao (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id           BIGINT      NOT NULL,
    caso_id              BIGINT      NOT NULL,
    horas_restantes      INT         NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    data_inicio          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fim             DATETIME    NULL,
    acusacao_suspeito_id BIGINT      NULL,
    acusacao_arma_id     BIGINT      NULL,
    acusacao_motivacao_id BIGINT     NULL,
    pontuacao            INT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sessao_usuario     FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_sessao_caso        FOREIGN KEY (caso_id)    REFERENCES caso (id),
    CONSTRAINT fk_sessao_ac_suspeito FOREIGN KEY (acusacao_suspeito_id)  REFERENCES suspeito (id),
    CONSTRAINT fk_sessao_ac_arma     FOREIGN KEY (acusacao_arma_id)      REFERENCES arma (id),
    CONSTRAINT fk_sessao_ac_motiv    FOREIGN KEY (acusacao_motivacao_id) REFERENCES motivacao (id),
    CONSTRAINT ck_sessao_status CHECK (status IN ('EM_ANDAMENTO','SOLUCIONADO','FALHOU'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Progresso salvo: depoimentos já desbloqueados na sessão
CREATE TABLE sessao_depoimento (
    sessao_id       BIGINT   NOT NULL,
    depoimento_id   BIGINT   NOT NULL,
    data_desbloqueio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sessao_id, depoimento_id),
    CONSTRAINT fk_sd_sessao     FOREIGN KEY (sessao_id)     REFERENCES sessao_investigacao (id) ON DELETE CASCADE,
    CONSTRAINT fk_sd_depoimento FOREIGN KEY (depoimento_id) REFERENCES depoimento (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Progresso salvo: pistas já descobertas na sessão
CREATE TABLE sessao_pista (
    sessao_id         BIGINT   NOT NULL,
    pista_id          BIGINT   NOT NULL,
    data_descoberta   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sessao_id, pista_id),
    CONSTRAINT fk_sp_sessao FOREIGN KEY (sessao_id) REFERENCES sessao_investigacao (id) ON DELETE CASCADE,
    CONSTRAINT fk_sp_pista  FOREIGN KEY (pista_id)  REFERENCES pista (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_suspeito_caso   ON suspeito (caso_id);
CREATE INDEX idx_pista_caso      ON pista (caso_id);
CREATE INDEX idx_depoimento_susp ON depoimento (suspeito_id);
CREATE INDEX idx_sessao_usuario  ON sessao_investigacao (usuario_id);
CREATE INDEX idx_usuario_pontos  ON usuario (pontuacao_geral DESC);
