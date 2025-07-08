package com.example.checklistkotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView // elemento para mostrar a lista de tarefas.
    private lateinit var inputTask: EditText // campo de texto para digitar a tarefa.
    private lateinit var addButton: Button // botão para adicionar novas tarefas.

    private lateinit var filterPending: TextView
    private lateinit var filterDone: TextView
    private lateinit var filterAll: TextView

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

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

    // Marca a aba atual como selecionada (opcional)
            bottomNavigation.selectedItemId = R.id.nav_tasks

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_tasks -> {
                        // Já está na tela de tarefas
                        true
                    }
                    R.id.nav_stats -> {
                        val intent = Intent(this, StatisticsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }


        // Referencia views do XML
        listView = findViewById(R.id.taskListView)
        addButton = findViewById(R.id.addButton)
        inputTask = findViewById(R.id.inputTask)

        filterPending = findViewById(R.id.filterPending)
        filterDone = findViewById(R.id.filterDone)
        filterAll = findViewById(R.id.filterAll)

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

        applyFilter()
        highlightFilter()

        //Um listener é configurado para o botão de adicionar.
        addButton.setOnClickListener {
            val text = inputTask.text.toString().trim() //Ao clicar em “Adicionar”, lemos o conteúdo de inputTask e eliminamos espaços extras.
            if (text.isNotEmpty()) {    // Quando clicado, ele verifica se o campo de texto não está vazio.
                tasks.add(Task(text)) // Cria um novo Task(text, done=false) e adiciona à lista tasks.
                inputTask.text.clear() // Limpa o campo de entrada.
                taskStorage.saveTasks(tasks) // Chama taskStorage.saveTasks(tasks) para persistir.
                applyFilter() // Reaplica o filtro e notifica o adapter para atualizar a tela.
            } else {
                Toast.makeText(this, "Digite uma tarefa para adicionar.", Toast.LENGTH_SHORT).show() // Caso contrário, uma mensagem de erro é exibida
            }
        }

        filterPending.setOnClickListener {
            currentFilter = 2
            applyFilter()
            highlightFilter()
        }

        filterDone.setOnClickListener {
            currentFilter = 1
            applyFilter()
            highlightFilter()
        }

        filterAll.setOnClickListener {
            currentFilter = 0
            applyFilter()
            highlightFilter()
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

    private fun highlightFilter() {
        val defaultColor = getColor(R.color.textPrimary)
        val selectedColor = getColor(R.color.primary)

        filterAll.setTextColor(if (currentFilter == 0) selectedColor else defaultColor)
        filterDone.setTextColor(if (currentFilter == 1) selectedColor else defaultColor)
        filterPending.setTextColor(if (currentFilter == 2) selectedColor else defaultColor)
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
                    Toast.makeText(this, "A tarefa não pode estar vazia.", Toast.LENGTH_SHORT)
                        .show()
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
                    setBackgroundColor(getColor(R.color.surface))
                    setPadding(16, 16, 16, 16)
                    layoutParams = AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.WRAP_CONTENT
                    )
                }

                val checkBox = CheckBox(this@MainActivity).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    textSize = 18f
                    setTextColor(getColor(R.color.textPrimary))
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
                // Identifica qual tarefa deve ser removida, baseando-se na lista atualmente exibida (filtrada).
                val taskToRemove = filteredTasks[position]
                // Remove esse mesmo objeto da lista completa de tarefas.
                tasks.remove(taskToRemove)

                // Chama o filtro para atualizar a lista exibida, assim
                // removendo visualmente o item da tela.
                applyFilter()

                // Persiste essa mudança salvando a lista atualizada
                // no SharedPreferences via TaskStorage.
                taskStorage.saveTasks(tasks)
            }

            // editar tarefa com clique longo no item da lista
            view.setOnClickListener {
                // Recupera o objeto Task que está sendo exibido naquela posição filtrada
                val taskToEdit = filteredTasks[position]

                // Busca na lista completa (“tasks”) o índice desse mesmo objeto em memória
                // Usamos === (referência exata) para garantir que é o mesmo objeto em memória,
                // não apenas um clone com valores iguais. Retorna a posição do primeiro elemento cujo
                //referencial (===) seja o mesmo de taskToEdit
                // Se não encontrar, indexOfFirst retorna -1.
                val originalPosition = tasks.indexOfFirst { it === taskToEdit }

                // Se encontrou um índice válido (>= 0), chama o diálogo de edição nesse índice
                if (originalPosition != -1) {
                    showEditDialog(originalPosition)
                }
                // Caso contrário, algo deu errado (objeto não achado) e é exibida uma mensagem de erro
                else {
                    Toast.makeText(this@MainActivity, "Erro ao editar tarefa", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            return view
        }

        //ViewHolder é uma classe interna que armazena referências para o CheckBox e o ImageButton,
        // melhorando a eficiência da lista ao evitar chamadas repetidas para findViewById.
        inner class ViewHolder(val checkBox: CheckBox, val deleteButton: ImageButton)
    }
}

