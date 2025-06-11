package com.example.checklistkotlin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson

//classe Task que armazena o texto e o status done: Boolean
data class Task(var text: String, var done: Boolean = false)

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView // elemento para mostrar a lista de tarefas.
    private lateinit var inputTask: EditText // campo de texto para digitar a tarefa.
    private lateinit var addButton: Button // botão para adicionar novas tarefas.
    private lateinit var filterSpinner: Spinner


    private lateinit var tasks: MutableList<Task> // lista que armazena as tarefas adicionadas.
    private lateinit var adapter: TaskAdapter
    private val gson = Gson()
    private var currentFilter = 0 // 0: Todas, 1: Concluídas, 2: Pendentes

    private lateinit var taskStorage: TaskStorage
    private var filteredTasks = mutableListOf<Task>()

    data class FilterItem(val iconResId: Int, val label: String)

    // Lista com ícones e labels para o spinner
    private val filterItems = listOf(
        FilterItem(R.drawable.ic_all, "Todas"),
        FilterItem(R.drawable.ic_done, "Concluídas"),
        FilterItem(R.drawable.ic_pending, "Pendentes")
    )

    //O método onCreate é chamado quando a Activity é criada.
    // Aqui, a interface do usuário é configurada.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //método usado no Android para definir qual layout XML será
        // exibido como a interface de usuário (UI) principal de uma Activity.
        //“Mostre a interface definida no arquivo activity_main.xml nesta tela (MainActivity).”
        setContentView(R.layout.activity_main)

        // Referencia views do XML
        listView = findViewById(R.id.taskListView)
        addButton = findViewById(R.id.addButton)
        inputTask = findViewById(R.id.inputTask)
        filterSpinner = findViewById(R.id.filterSpinner)

        // integração do app com o SharedPreferences usando a classe TaskStorage
        // passa a activity em instancia da TaskStorage
        // Isso é necessário porque o SharedPreferences precisa de um Context para funcionar.
        //Assim, poder salvar e carregar tarefas da memória persistente (armazenamento interno do app).
        taskStorage = TaskStorage(this)

        //Chama o método loadTasks() da TaskStorage que
        //Lê as tarefas salvas no SharedPreferences
        //Usa Gson para converter o JSON de volta em uma List<Task>
        // usa .toMutableList() para transformar a lista carregada em uma lista mutável, permitindo adicionar, editar e remover tarefas.
        tasks = taskStorage.loadTasks().toMutableList()

        adapter = TaskAdapter()
        listView.adapter = adapter

        setupSpinner()
        applyFilter()

        //Um listener é configurado para o botão de adicionar.
        // Quando clicado, ele verifica se o campo de texto não está vazio.
        // Se não estiver, a tarefa é adicionada à lista e a interface é atualizada.
        // Caso contrário, uma mensagem de erro é exibida.
        addButton.setOnClickListener {
            val text = inputTask.text.toString().trim()
            if (text.isNotEmpty()) {
                tasks.add(Task(text)) // Cria nova tarefa com done = false
                inputTask.text.clear()
                taskStorage.saveTasks(tasks) // salva ao adicionar
                applyFilter()
            } else {
                Toast.makeText(this, "Digite uma tarefa para adicionar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //funcao de filtro
    private fun setupSpinner() {
        val spinnerAdapter = object : ArrayAdapter<FilterItem>(
            this,
            R.layout.custom_spinner_item,
            filterItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return createCustomView(position, convertView, parent)
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return createCustomView(position, convertView, parent)
            }

            private fun createCustomView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = layoutInflater.inflate(R.layout.custom_spinner_item, parent, false)
                val icon = view.findViewById<ImageView>(R.id.spinnerIcon)
                val label = view.findViewById<TextView>(R.id.spinnerText)

                val item = getItem(position)
                icon.setImageResource(item?.iconResId ?: 0)
                label.text = item?.label ?: ""

                return view
            }
        }
        filterSpinner.adapter = spinnerAdapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentFilter = position
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    //funcao de filtro
    private fun applyFilter() {
        filteredTasks = when (currentFilter) {
            1 -> tasks.filter { it.done }.toMutableList()
            2 -> tasks.filter { !it.done }.toMutableList()
            else -> tasks.toMutableList()
        }
        adapter.notifyDataSetChanged()
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
                    taskStorage.saveTasks(tasks) // salva ao editar
                    applyFilter() // ← Corrige a lista filtrada após edição
                } else {
                    Toast.makeText(this, "A tarefa não pode estar vazia.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // TaskAdapter é uma classe interna que estende BaseAdapter. Ela fornece os métodos necessários para gerenciar a lista de tarefas.
    inner class TaskAdapter : BaseAdapter() {
        override fun getCount(): Int = filteredTasks.size

        override fun getItem(position: Int): Any = filteredTasks[position]

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

            val task = filteredTasks[position]
            viewHolder.checkBox.text = task.text

            // Remove listener antigo antes de mudar o estado para evitar disparo indesejado/callback duplicado
            viewHolder.checkBox.setOnCheckedChangeListener(null)
            viewHolder.checkBox.isChecked = task.done  // Define o estado sem disparar listener

            viewHolder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.done = isChecked
                taskStorage.saveTasks(tasks)
                applyFilter()
            }

            viewHolder.deleteButton.setOnClickListener {
                val taskToRemove = filteredTasks[position]
                tasks.remove(taskToRemove)
                applyFilter()
                taskStorage.saveTasks(tasks) // salva ao deletar
            }

            // editar tarefa com clique longo no item da lista
            view.setOnClickListener {
                val taskToEdit = filteredTasks[position]
                val originalPosition = tasks.indexOfFirst { it === taskToEdit }
                if (originalPosition != -1) {
                    showEditDialog(originalPosition)
                } else {
                    Toast.makeText(this@MainActivity, "Erro ao editar tarefa", Toast.LENGTH_SHORT).show()
                }
                true
            }

            return view
        }

        //ViewHolder é uma classe interna que armazena referências para o CheckBox e o ImageButton,
        // melhorando a eficiência da lista ao evitar chamadas repetidas para findViewById.
        inner class ViewHolder(val checkBox: CheckBox, val deleteButton: ImageButton)
    }
}
