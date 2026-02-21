-- ============================================
-- PoC Chat - Your Car Your Way
-- Script d'initialisation de la base de données
-- ============================================

CREATE DATABASE IF NOT EXISTS poc_chat
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE poc_chat;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    agent_id BIGINT,
    -- ✅ status ici, sur conversations
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_conversation_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_conversation_agent FOREIGN KEY (agent_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS chats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    -- ✅ pas de status ici
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_chat_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- ============================================
-- DONNÉES : utilisateurs de test
-- Mot de passe : password (BCrypt coût 10)
-- ============================================
INSERT INTO users (firstname, lastname, email, password, type, created_at, updated_at) VALUES
(
    'Jean',
    'Dupont',
    'client@test.com',
    '$2a$10$2O77ndhTxP9bgLYR2kCN9OqJrQA6tFYKP1tSnBNihEBiugVcsLLVK',
    'CLIENT',
    NOW(),
    NOW()
),
(
    'Marie',
    'Martin',
    'agent@test.com',
    '$2a$10$2O77ndhTxP9bgLYR2kCN9OqJrQA6tFYKP1tSnBNihEBiugVcsLLVK',
    'AGENT_SUPPORT',
    NOW(),
    NOW()
);

SELECT 'Base de données créée avec succès !' AS status;
SELECT id, firstname, lastname, email, type FROM users;