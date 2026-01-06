package com.example.javaproject.services;

import com.example.javaproject.dao.UserDAO;
import com.example.javaproject.dao.EmployeDAO;
import com.example.javaproject.models.Employe;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final EmployeDAO employeDAO = new EmployeDAO();

    // Logique pour vérifier si c'est un Admin
    public boolean isAdmin(String username, String password) {
        return userDAO.validate(username, password);
    }

    // Logique pour authentifier un Employé
    public Employe authenticateEmploye(String identifier, String password) {
        return employeDAO.authenticate(identifier, password);
    }
    public boolean registerNewUser(String username, String password, String confirm) throws IllegalArgumentException {
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Champs vides.");
        }
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        // ICI : On appelle le DAO des employés, pas celui des Admins !
        return employeDAO.registerEmploye(username, password);
    }

    // Logique d'inscription
    public boolean registerAdmin(String username, String password, String confirm) throws IllegalArgumentException {
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Champs vides.");
        }
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
        return userDAO.register(username, password);
    }

    // Logique de récupération
    public boolean resetPassword(String username, String newPass) {
        return userDAO.updatePassword(username, newPass);
    }
}

