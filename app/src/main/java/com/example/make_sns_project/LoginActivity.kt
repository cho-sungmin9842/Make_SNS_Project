package com.example.make_sns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.make_sns_project.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.login.setOnClickListener {
            val userEmail = binding.username.text.toString()
            val password = binding.password.text.toString()
            if(userEmail.isEmpty()||password.isEmpty())
            {
                Snackbar.make(binding.root,"이메일과 패스워드를 입력해주세요.",Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doLogin(userEmail, password)
        }
        binding.signup.setOnClickListener{
            val userEmail = binding.username.text.toString()
            val password = binding.password.text.toString()
            if(userEmail.isEmpty()||password.isEmpty())
            {
                Snackbar.make(binding.root,"이메일과 패스워드를 입력해주세요.",Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Firebase.auth.createUserWithEmailAndPassword(userEmail, password).addOnCompleteListener(this) {
                if (it.isSuccessful)
                {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else
                {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(this) {
            if (it.isSuccessful)
            {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            else
            {
                Log.w("LoginActivity", "signInWithEmail", it.exception)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}