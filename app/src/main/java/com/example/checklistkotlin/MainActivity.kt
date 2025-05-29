package com.example.checklistkotlin


import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

//classe Task que armazena o texto e o status done: Boolean
data class Task(var text: String, var done: Boolean = false)

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView // elemento para mostrar a lista de tarefas.
    private lateinit var addButton: Button // botão para adicionar novas tarefas.
    private lateinit var inputTask: EditText // campo de texto para digitar a tarefa.
    private val tasks = ArrayList<Task>() // lista que armazena as tarefas adicionadas.
    private lateinit var adapter: TaskAdapter

    //O método onCreate é chamado quando a Activity é criada.
    // Aqui, a interface do usuário é configurada.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usa seu layout XML
        setContentView(R.layout.activity_main)


        // Referencia views do XML
        listView = findViewById(R.id.taskListView)
        addButton = findViewById(R.id.addButton)
        inputTask = findViewById(R.id.inputTask)

        adapter = TaskAdapter()
        listView.adapter = adapter

        adapter = TaskAdapter()
        listView.adapter = adapter

        //Um listener é configurado para o botão de adicionar.
        // Quando clicado, ele verifica se o campo de texto não está vazio.
        // Se não estiver, a tarefa é adicionada à lista e a interface é atualizada.
        // Caso contrário, uma mensagem de erro é exibida.
        addButton.setOnClickListener {
            val text = inputTask.text.toString().trim()
            if (text.isNotEmpty()) {
                tasks.add(Task(text)) // Cria nova tarefa com done = false
                adapter.notifyDataSetChanged()
                inputTask.text.clear()
            } else {
                Toast.makeText(this, "Digite uma tarefa para adicionar.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //Ao clicar em qualquer item da lista, uma caixa de diálogo aparecerá com a tarefa atual preenchida.
    //Você pode editar e salvar ou cancelar.
    //O ListView será atualizado automaticamente.
    private fun showEditDialog(position: Int) {
        val editText = EditText(this).apply {
            setText(tasks[position].text)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar Tarefa")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    tasks[position].text = newText
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "A tarefa não pode estar vazia.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // TaskAdapter é uma classe interna que estende BaseAdapter. Ela fornece os métodos necessários para gerenciar a lista de tarefas.
    inner class TaskAdapter : BaseAdapter() {
        override fun getCount(): Int = tasks.size

        override fun getItem(position: Int): Any = tasks[position]

        override fun getItemId(position: Int): Long = position.toLong()

        //O método getView é responsável por criar ou reutilizar as visualizações para cada item da lista.
        // Ele cria um layout com um CheckBox e um ImageButton para deletar a tarefa.
        //Quando o botão de deletar é clicado, a tarefa correspondente é removida da lista.
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewHolder: ViewHolder
            val view: View

            if (convertView == null) {
                val itemLayout = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(16, 16, 16, 16)
                    layoutParams = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
                }

                val checkBox = CheckBox(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    textSize = 18f
                }

                val deleteButton = ImageButton(this@MainActivity).apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }


                itemLayout.addView(checkBox)
                itemLayout.addView(deleteButton)

                view = itemLayout
                viewHolder = ViewHolder(checkBox, deleteButton)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            val task = tasks[position]
            viewHolder.checkBox.text = task.text

            // Remove listener antigo antes de mudar o estado para evitar disparo indesejado/callback duplicado
            viewHolder.checkBox.setOnCheckedChangeListener(null)
            viewHolder.checkBox.isChecked = task.done  // Define o estado sem disparar listener

            viewHolder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.done = isChecked // Atualiza o objeto
            }

            viewHolder.deleteButton.setOnClickListener {
                tasks.removeAt(position)
                notifyDataSetChanged()
            }

            viewHolder.checkBox.setOnClickListener {
                showEditDialog(position)
                true
            }

            return view
        }

        //ViewHolder é uma classe interna que armazena referências para o CheckBox e o ImageButton,
        // melhorando a eficiência da lista ao evitar chamadas repetidas para findViewById.
        inner class ViewHolder(val checkBox: CheckBox, val deleteButton: ImageButton)
    }
}
