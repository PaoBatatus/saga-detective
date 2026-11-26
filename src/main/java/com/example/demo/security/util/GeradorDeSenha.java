package com.example.demo.security.util;

import java.util.Scanner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorDeSenha {

    public static void main(String[] args) {
        try (Scanner teclado = new Scanner(System.in)) {
            System.out.print("Digite a senha: ");
            String senha = teclado.nextLine();
            System.out.println(new BCryptPasswordEncoder().encode(senha));
        }
    }
}