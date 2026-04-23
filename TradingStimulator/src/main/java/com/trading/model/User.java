package com.trading.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity // Designates class as a JPA entity that maps to a database record
@Table(name = "users") // Maps specifically to the "users" table
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends BaseEntity {

    // Ensures the username is strictly unique across the database
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash; // Stores the encrypted BCrypt hash of the password, NEVER plain text

    @Column(nullable = false)
    private double balance; // The user's simulated trading cash balance

    // Security questions for password resets
    private String securityQuestion;
    private String securityAnswer;

    // Required default constructor for JPA instantiation
    public User() {
    }

    public User(String username, String passwordHash, double balance, String securityQuestion, String securityAnswer) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.balance = balance;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
}
