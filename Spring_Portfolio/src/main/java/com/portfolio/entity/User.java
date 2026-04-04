package com.portfolio.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "\"user\"")
@Data
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "user_name", nullable = false) // 對應資料庫欄位 user_name
	private String userName;

	@Column(name = "password") // 對應資料庫欄位 password
	private String password;

	@Column(name = "active")
	private boolean active;

	@Column(name = "roles")
	private String roles;
}
