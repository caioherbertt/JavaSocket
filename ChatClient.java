import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient extends JFrame {
    private JTextArea CampoExibeTexto;
    private JTextField CampoTexto;
    private PrintWriter out;

    public ChatClient() {
        setTitle("Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Painel para exibir mensagens
        CampoExibeTexto = new JTextArea();
        CampoExibeTexto.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(CampoExibeTexto);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Painel para entrada de mensagem
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        CampoTexto = new JTextField();
        JButton BotaoEnvio = new JButton("Enviar");

        // Configuração do ActionListener para o botão de envio
        BotaoEnvio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String texto = CampoTexto.getText();
                enviaMensagem(texto);
                exibeMensagem("Você: " + texto);
                CampoTexto.setText("");
            }
        });

        inputPanel.add(CampoTexto, BorderLayout.CENTER);
        inputPanel.add(BotaoEnvio, BorderLayout.EAST);

        // Adicionando os painéis ao frame principal
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Solicita ao usuário que insira um nome
        String name = JOptionPane.showInputDialog(this, "Insira seu nome:", "Identificação", JOptionPane.PLAIN_MESSAGE);

        // Conecta ao servidor na porta 12345
        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Envia o nome do cliente para o servidor
            out.println(name);

            // Exibe o nome do cliente
            exibeMensagem("Você é: " + name);

            // Cria uma thread para receber mensagens do servidor
            Thread receiveThread = new Thread(() -> {
                try {
                    String texto;
                    while ((texto = in.readLine()) != null) {
                        exibeMensagem(texto);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para exibição do texto no JTextArea
    public void exibeMensagem(String texto) {
        SwingUtilities.invokeLater(() -> CampoExibeTexto.append(texto + "\n"));
    }

    // Método responsável pelo envio do texto para o servidor
    public void enviaMensagem(String texto) {
        out.println(texto);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatClient clientGUI = new ChatClient();
                clientGUI.setVisible(true);
            }
        });
    }
}