package com.example.checklistkotlin


import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import android.view.ViewGroup


class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView // elemento para mostrar a lista de tarefas.
    private lateinit var addButton: Button // botão para adicionar novas tarefas.
    private lateinit var inputTask: EditText // campo de texto para digitar a tarefa.
    private val tasks = ArrayList<String>() // lista que armazena as strings das tarefas adicionadas.
    private lateinit var adapter: TaskAdapter

    //O método onCreate é chamado quando a Activity é criada.
    // Aqui, a interface do usuário é configurada.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cria um layout vertical que será o container principal da tela, com padding 32 pixels.
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }

        // Cria um título na parte superior e adiciona ao layout principal.
        val title = TextView(this).apply {
            text = "Checklist de Atividades Diárias"
            textSize = 24f
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(title)

        // Um layout horizontal para o campo de texto e o botão de adicionar lado a lado.
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        // Campo onde o usuário digita sua nova tarefa, ocupando o máximo de espaço possível na horizontal.
        inputTask = EditText(this).apply {
            hint = "Adicionar nova tarefa"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            inputType = InputType.TYPE_CLASS_TEXT
        }

        //Botão para adicionar a tarefa digitada
        addButton = Button(this).apply {
            text = "Adicionar"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        //O campo de texto e o botão são adicionados ao layout de entrada, que por sua vez é adicionado ao layout principal.
        inputLayout.addView(inputTask)
        inputLayout.addView(addButton)

        rootLayout.addView(inputLayout)

        // Um ListView é criado para exibir a lista de tarefas e adicionado ao layout principal.
        listView = ListView(this)
        rootLayout.addView(listView)

        // O layout principal é definido como a visualização da Activity.
        // Um adaptador (TaskAdapter) é criado e associado ao ListView.
        setContentView(rootLayout)

        adapter = TaskAdapter()
        listView.adapter = adapter

        //Um listener é configurado para o botão de adicionar.
        // Quando clicado, ele verifica se o campo de texto não está vazio.
        // Se não estiver, a tarefa é adicionada à lista e a interface é atualizada.
        // Caso contrário, uma mensagem de erro é exibida.
        addButton.setOnClickListener {
            val taskText = inputTask.text.toString().trim()
            if (taskText.isNotEmpty()) {
                tasks.add(taskText)
                adapter.notifyDataSetChanged()
                inputTask.text.clear()
            } else {
                Toast.makeText(this, "Digite uma tarefa para adicionar.", Toast.LENGTH_SHORT).show()
            }
        }
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
                    setBackgroundResource(0)
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
            viewHolder.checkBox.text = task
            viewHolder.checkBox.isChecked = false
            viewHolder.deleteButton.setOnClickListener {
                tasks.removeAt(position)
                notifyDataSetChanged()
            }

            return view
        }

        //ViewHolder é uma classe interna que armazena referências para o CheckBox e o ImageButton,
        // melhorando a eficiência da lista ao evitar chamadas repetidas para findViewById.
        inner class ViewHolder(val checkBox: CheckBox, val deleteButton: ImageButton)
    }
}
