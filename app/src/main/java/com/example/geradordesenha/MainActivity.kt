package com.example.geradordesenha

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {

    private lateinit var textViewPasswordLength: TextView
    private lateinit var seekBarPasswordLength: SeekBar
    private lateinit var checkBoxUppercase: CheckBox
    private lateinit var checkBoxLowercase: CheckBox
    private lateinit var checkBoxNumbers: CheckBox
    private lateinit var checkBoxExcludeSimilar: CheckBox
    private lateinit var textViewPassword: TextView
    private lateinit var buttonRegenerate: Button
    private lateinit var buttonCopy: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewPasswordLength = findViewById(R.id.textViewPasswordLength)
        seekBarPasswordLength = findViewById(R.id.seekBarPasswordLength)
        checkBoxUppercase = findViewById(R.id.checkBoxUppercase)
        checkBoxLowercase = findViewById(R.id.checkBoxLowercase)
        checkBoxNumbers = findViewById(R.id.checkBoxNumbers)
        checkBoxExcludeSimilar = findViewById(R.id.checkBoxExcludeSimilar)
        textViewPassword = findViewById(R.id.textViewPassword)
        buttonRegenerate = findViewById(R.id.buttonRegenerate)
        buttonCopy = findViewById(R.id.buttonCopy)

        // Configuração inicial
        checkBoxLowercase.isChecked = true // Por padrão, começa com minúsculas habilitado
        updatePasswordLengthText(seekBarPasswordLength.progress)
        generatePassword()

        seekBarPasswordLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePasswordLengthText(progress)
                generatePassword()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        buttonRegenerate.setOnClickListener {
            generatePassword()
        }

        buttonCopy.setOnClickListener {
            copyPasswordToClipboard()
        }

        // Adiciona listeners para os checkboxes para gerar nova senha ao mudar estado
        val checkBoxes = listOf(checkBoxUppercase, checkBoxLowercase, checkBoxNumbers, checkBoxExcludeSimilar)
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                generatePassword()
            }
        }
    }

    private fun updatePasswordLengthText(length: Int) {
        textViewPasswordLength.text = "Comprimento da Senha: $length"
    }

    private fun generatePassword() {
        if (!checkBoxUppercase.isChecked && !checkBoxLowercase.isChecked && !checkBoxNumbers.isChecked) {
            textViewPassword.text = "Selecione uma opção"
            Toast.makeText(this, "Selecione ao menos um tipo de caractere.", Toast.LENGTH_SHORT).show()
            return
        }

        val length = seekBarPasswordLength.progress
        val includeUppercase = checkBoxUppercase.isChecked
        val includeLowercase = checkBoxLowercase.isChecked
        val includeNumbers = checkBoxNumbers.isChecked
        val excludeSimilar = checkBoxExcludeSimilar.isChecked

        val password = PasswordGenerator.generatePassword(
            length,
            includeUppercase,
            includeLowercase,
            includeNumbers,
            excludeSimilar
        )
        textViewPassword.text = password
    }

    private fun copyPasswordToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Password", textViewPassword.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Senha copiada para a área de transferência!", Toast.LENGTH_SHORT).show()
    }
}

object PasswordGenerator {

    private const val UPPERCASE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ" // Excluído I, O
    private const val LOWERCASE_CHARS = "abcdefghijkmnopqrstuvwxyz" // Excluído l
    private const val NUMBER_CHARS = "23456789" // Excluído 0, 1
    private const val ALL_UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val ALL_LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
    private const val ALL_NUMBER_CHARS = "0123456789"

    fun generatePassword(
        length: Int,
        includeUppercase: Boolean,
        includeLowercase: Boolean,
        includeNumbers: Boolean,
        excludeSimilarCharacters: Boolean
    ): String {
        if (length <= 0) return ""

        val charPool = StringBuilder()
        if (includeUppercase) {
            charPool.append(if (excludeSimilarCharacters) UPPERCASE_CHARS else ALL_UPPERCASE_CHARS)
        }
        if (includeLowercase) {
            charPool.append(if (excludeSimilarCharacters) LOWERCASE_CHARS else ALL_LOWERCASE_CHARS)
        }
        if (includeNumbers) {
            charPool.append(if (excludeSimilarCharacters) NUMBER_CHARS else ALL_NUMBER_CHARS)
        }

        if (charPool.isEmpty()) {
            return "Erro: Nenhum conjunto de caracteres selecionado."
        }

        val random = SecureRandom()
        val password = StringBuilder(length)

        for (i in 0 until length) {
            val randomIndex = random.nextInt(charPool.length)
            password.append(charPool[randomIndex])
        }
        return password.toString()
    }
}