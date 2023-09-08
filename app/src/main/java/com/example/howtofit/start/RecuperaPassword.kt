package com.example.howtofit.start

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.howtofit.R
import com.google.firebase.auth.FirebaseAuth


class RecuperaPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recupera_password)

        val editEmailRecupero: EditText = findViewById(R.id.EmailRecupero)
        val buttonTornaAlLogin: Button = findViewById(R.id.buttonTornaAlLoginRecupero)
        val buttonCambiaPassword: Button = findViewById(R.id.buttonCambiaPsw)



        buttonTornaAlLogin.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun controllaSeVuoto(): Boolean {
            if (editEmailRecupero.text.toString().isEmpty()){
                Toast.makeText(
                    baseContext,
                    "Devi ancora inserire la Mail",
                    Toast.LENGTH_LONG,
                ).show()
                return false
            }
            else{
                return true
            }
        }

        buttonCambiaPassword.setOnClickListener(){
            if (controllaSeVuoto()){

                val emailPerRecupero = editEmailRecupero.text.toString()

                FirebaseAuth.getInstance().sendPasswordResetEmail(emailPerRecupero)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                baseContext,
                                "Controlla la tua Mail per recuperare la password",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                baseContext,
                                "C'è stato un errore nell'invio della mail",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


            }
        }

    }
}