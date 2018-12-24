package kvv.education.khasang.java1.chat.views.gui;

import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.*;
import java.util.List;
/*
   JTabbedPane tabbedPane;
       JPanel chatPanel;
            JSplitPane
                 JPanel
                    JSplitPane
                           JPanel mainDialogPanel
                               JLabel panelDialogLabel
                               JPanel dialogsPanel;
                                   JComboBox<Dialog> jComboBoxDialogs
                                   ButtonGroup buttonGroup;
                                       java.util.List<DialogRadioButton> dialogRadioButtons;
                                JButton createDialogButton
                           JPanel mainUserPanel;
                               JLabel panelUsersLabel
                               JPanel usersPanel;
                                   java.util.List<JLabel> dialogUserLabels;
                               JButton addUserToDialogButton;
                  JPanel mainPanel;
                         JLabel currentInfoLabel;
                         JTextPane messagesTextPane;
                         JTextArea currentMessage;
                         JButton sendMessageButton;
       JPanel settingsPanel;
           JTextField userField;
           JPasswordField passwordField;
           JButton enterButton;
           JTextField periodField
   JLabel errorLabel;
 */

/**
 * View для диалогового варианта чата
 */
public class WindowViewChat extends JFrame {//implements IOChat {

    private JTabbedPane tabbedPane;
    private JPanel chatPanel;
    private JPanel mainDialogPanel;
    private JPanel dialogsPanel;
    private JLabel panelDialogLabel;
    private JComboBox<kvv.education.khasang.java1.chat.model.basic_entity.Dialog> jComboBoxDialogs;
    private ButtonGroup buttonGroup;
    private java.util.List<DialogRadioButton> dialogRadioButtons;
    private JButton createDialogButton;
    private JPanel mainUserPanel;
    private JLabel panelUsersLabel;
    private JPanel usersPanel;
    private java.util.List<UserLabel> dialogUserLabels;
    private JButton addUserToDialogButton;
    private JPanel mainPanel;
    private JLabel currentInfoLabel;
    private JTextPane messagesTextPane;
    private JTextArea currentMessage;
    private JButton sendMessageButton;
    private JPanel settingsPanel;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton enterButton;
    private JCheckBox isNewUser;
    private JLabel errorLabel;

    WindowControllerChat windowControllerChat;

    private static final User UNDEFINED_USER = null;
    private static final kvv.education.khasang.java1.chat.model.basic_entity.Dialog UNDEFINED_DIALOG = null;
    private User currentUser;
    private kvv.education.khasang.java1.chat.model.basic_entity.Dialog currentDialog;

    private static final boolean DEFAULT_DO_LISTEN_NEW_MSGS = true;
    //флаг осуществлять ли прослушку на наличие новых сообщений
    private boolean doListenNewMessages;
    //Поток, отправляющий контролеру задания на обновление окна сообщений
    private Thread newsListener;
    //Период обновления окна сообщений.
    private static final long UPDATE_MSG_PERIOD = 1000;
    //суффикс отображаемый в заголовке окна чата
    private String titleSuffix;

    public WindowViewChat() throws HeadlessException {
        currentUser = UNDEFINED_USER;
        currentDialog = UNDEFINED_DIALOG;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        createGui();
        updateStatus();
        setMinimumSize(new Dimension(600, 400));
        setVisible(true);
        setDoListenNewMessages(true);

    }

    private void createGui() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        errorLabel = new JLabel("");
        errorLabel.setForeground(WindowTitles.COLOR_ERROR);
        add(errorLabel, BorderLayout.SOUTH);

        chatPanel = new JPanel();
        settingsPanel = new JPanel();
        settingsPanel.setBackground(WindowTitles.COLOR_CHAT_PANEL);
        tabbedPane.add(WindowTitles.TITLE_PANEL1, chatPanel);
        tabbedPane.add(WindowTitles.TITLE_PANEL2, settingsPanel);
        tabbedPane.setSelectedIndex(1);

        //создание панели связанной с выбором диалогов
        mainDialogPanel = new JPanel(new BorderLayout());
        mainDialogPanel.setBackground(WindowTitles.COLOR_CHAT_PANEL);

        panelDialogLabel = new JLabel(WindowTitles.TITLE_DIALOGS);
        panelDialogLabel.setHorizontalAlignment(JLabel.CENTER);
        panelDialogLabel.setFont(WindowTitles.FONT_TITLE_PANEL);
        panelDialogLabel.setForeground(WindowTitles.COLOR_LABEL_PANEL);
        mainDialogPanel.add(panelDialogLabel, BorderLayout.NORTH);

        createDialogButton = new JButton(WindowTitles.TITLE_BUTTON_CREATE_DIALOG);
        mainDialogPanel.add(createDialogButton, BorderLayout.SOUTH);

        dialogsPanel = new JPanel();
        dialogsPanel.setLayout(new BoxLayout(dialogsPanel, BoxLayout.Y_AXIS));

        jComboBoxDialogs = new JComboBox<>();
        jComboBoxDialogs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        dialogsPanel.add(jComboBoxDialogs);

        JScrollPane jScrollDialogPanel = new JScrollPane(dialogsPanel);
        mainDialogPanel.add(jScrollDialogPanel, BorderLayout.CENTER);

        //создание панели связанной с участниками беседы
        mainUserPanel = new JPanel(new BorderLayout());
        panelUsersLabel = new JLabel(WindowTitles.TITLE_USERS);
        panelUsersLabel.setFont(WindowTitles.FONT_TITLE_PANEL);
        panelUsersLabel.setForeground(WindowTitles.COLOR_LABEL_PANEL);
        panelUsersLabel.setHorizontalAlignment(JLabel.CENTER);
        mainUserPanel.add(panelUsersLabel, BorderLayout.NORTH);
        usersPanel = new JPanel();
        JScrollPane jScrollUserPanel = new JScrollPane(usersPanel);
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        mainUserPanel.add(jScrollUserPanel, BorderLayout.CENTER);
        mainUserPanel.setBackground(WindowTitles.COLOR_CHAT_PANEL);
        addUserToDialogButton = new JButton(WindowTitles.TITLE_BUTTON_ADD_USER);
        mainUserPanel.add(addUserToDialogButton, BorderLayout.SOUTH);


        //создание панели информации о беседе
        mainPanel = new JPanel();
        chatPanel.setBackground(WindowTitles.COLOR_CHAT_MAIN);
        chatPanel.setLayout(new BorderLayout());
        //--------------

        JSplitPane jsp1 = new JSplitPane();
        jsp1.setDividerSize(5);
        jsp1.setResizeWeight(0.1);
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(jsp1);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jsp1.setLeftComponent(jPanel);
        jsp1.setRightComponent(mainPanel);

        JSplitPane jsp2 = new JSplitPane();
        jsp2.setDividerSize(7);
        jsp2.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jsp2.setRightComponent(mainUserPanel);
        jsp2.setLeftComponent(mainDialogPanel);
        jPanel.add(jsp2);
        jsp2.setResizeWeight(0.8);

        //-----------
        dialogsPanel.setBackground(WindowTitles.COLOR_PANEL_DLGS);
        usersPanel.setBackground(WindowTitles.COLOR_PANEL_DLGS);
        mainPanel.setBackground(WindowTitles.COLOR_CHAT_PANEL);

        buildingDialogsPanel(null);
        dialogUserLabels = new ArrayList<>();
        buildingUsersPanel(null);
        buildingMainPanel();
        buildingSettingsPanel();
        buildingListeners();
    }

    /**
     * Устанавливает суффикс для заголовка окна, при этом обновляет заголовок окна
     *
     * @param suffix
     */
    public void setTitleSuffix(String suffix) {
        this.titleSuffix = suffix;
        setTitle(getTitle() + " " + suffix);
    }

    public void showTitleWithSuffix(String title) {
        if (titleSuffix == null) {
            this.setTitle(title);
        } else {
            this.setTitle(title + " " + titleSuffix);
        }
    }

    /**
     * Вспомогательный класс. Цель - связать JLabel и UserInChat
     */
    private class UserLabel extends JLabel {
        UserInChat userInChat;

        UserLabel(UserInChat userInChat) {
            super(userInChat.getUser().getLogin());
            this.userInChat = userInChat;
            changeStatus(this, userInChat.getStatus());
        }
    }

    /**
     * Визуальные измененения происходящие при изменении статуса пользователя
     *
     * @param userLabel
     * @param newStatus
     */
    private void changeStatus(UserLabel userLabel, UserStatus newStatus) {
        Color newColor;
        if (newStatus == UserStatus.OFFLINE) {
            userLabel.setIcon(WindowTitles.ICON_STATUS_USER_OFF);
            newColor = WindowTitles.COLOR_USER_OFFLINE;
        } else if (newStatus == UserStatus.ONLINE) {
            userLabel.setIcon(WindowTitles.ICON_STATUS_USER_ON);
            newColor = WindowTitles.COLOR_USER_ONLINE;
        } else {
            userLabel.setIcon(WindowTitles.ICON_STATUS_USER_UNDEF);
            newColor = WindowTitles.COLOR_USER_UNDEF_STATUS;
        }
        changeColorWithFlicker(userLabel, newColor, Color.WHITE);
    }

    /**
     * Вспомогательный класс - цель связать JRadioButton и Dialog
     */
    private class DialogRadioButton extends JRadioButton {
        kvv.education.khasang.java1.chat.model.basic_entity.Dialog dialog;
    }

    public void cleanPassword() {
        passwordField.setText("");
    }

    /**
     * Принимает события произошедшие в моделе.
     *
     * @param event        событие
     * @param contextEvent контекст события
     */
    public void takeEvent(Event event, Object... contextEvent) {
        processingEvent(event, contextEvent);
    }

    public void showMessage(int messageType, String title, String text) {
        JOptionPane.showMessageDialog(this, text, title, messageType);
    }

    public void setWindowControllerChat(WindowControllerChat windowControllerChat) {
        this.windowControllerChat = windowControllerChat;
    }

    public WindowControllerChat getWindowControllerChat() {
        return windowControllerChat;
    }

    /**
     * Показывает информацию
     *
     * @param text
     * @param color
     */
    public void showCurrentInfo(String text, Color color) {
        currentInfoLabel.setForeground(color);
        currentInfoLabel.setText(text);
    }

    public void showError(String text) {
        errorLabel.setText(text);
    }

    public void cleanErrorInfo() {
        showError("");
    }

    public void cleanCurrentMessage() {
        currentMessage.setText("");
    }

    public void cleanCurrentAreaMessages() {
        messagesTextPane.setText("");
    }

    /*
    Проверка смены статуса пользователя. На след. реализацию чата через сокеты
    private void test() {
        if (dialogUserLabels.size() > 0) {
            UserLabel jLabel = dialogUserLabels.get(0);
            changeStatus(jLabel.userInChat.getUser(), UserStatus.OFFLINE);
        }
        messagesTextPane.scrollRectToVisible(new Rectangle(0, messagesTextPane.getWidth() - 2, 1, 1));
    }
    */

    /**
     * Изменение отображения при изменении статуса пользователя
     *
     * @param user
     * @param newStatus
     */
    private void changeStatus(User user, UserStatus newStatus) {
        for (UserLabel userLabel : dialogUserLabels) {
            if (userLabel.userInChat.getUser().equals(user)) {
                changeStatus(userLabel, newStatus);
            }
        }
    }

    /**
     * Измененение цвета JLabel через мерцание
     *
     * @param jLabel
     * @param colorFinish конечный цвет
     * @param color       промежуточный цвет
     */
    private void changeColorWithFlicker(JLabel jLabel, Color colorFinish, Color color) {
        Thread thread = new Thread(() -> {
            int count = 20;
            int i = 0;
            Color currentColor = null;
            while (i < count) {
                jLabel.setForeground(currentColor);
                currentColor = (Objects.equals(currentColor, colorFinish)) ? color : colorFinish;
                i++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void addListenerOnSendMessage() {
        WindowViewChat jFrame = this;
        JTextArea currentMessage = this.currentMessage;
        sendMessageButton.addActionListener(e -> {
            //отправляем только не пустые сообщения
            if (!currentMessage.getText().isEmpty()) {
                sendEventToController(Event.VIEW_SEND_MESSAGE, jFrame.currentMessage.getText());
            }
        });
    }

    private void addListenerOnEnter() {
        WindowViewChat jFrame = this;
        enterButton.addActionListener(e -> {
            if (!jFrame.isNewUser.isSelected()) {
                sendEventToController(Event.VIEW_DO_ENTER_USER, userField.getText(), passwordField.getPassword());
            } else {
                sendEventToController(Event.VIEW_CREATE_NEW_USER, jFrame.userField.getText(), String.valueOf(jFrame.passwordField.getPassword()));
            }
        });
    }

    private void addListenerOnCreateDialog() {
        WindowViewChat jFrame = this;
        createDialogButton.addActionListener(e -> {
            if (currentUser != UNDEFINED_USER) {
                String nameDialog = JOptionPane.showInputDialog(jFrame, WindowTitles.TITLE_MSG_NEW_DIALOG, WindowTitles.TITLE_WINDOW_NEW_DIALOG, JOptionPane.OK_CANCEL_OPTION);
                if (!(Objects.equals(nameDialog, null)) && (!(Objects.equals(nameDialog, "")))) {
                    sendEventToController(Event.VIEW_CREATE_DIALOG, nameDialog);
                }
            }
        });
    }

    private void addListenerOnAddUserToDialog() {
        WindowViewChat jFrame = this;
        addUserToDialogButton.addActionListener(e -> {
            if (currentDialog != UNDEFINED_DIALOG) {
                String login = JOptionPane.showInputDialog(jFrame, WindowTitles.TITLE_MSG_ADD_USER, WindowTitles.TITLE_WINDOW_ADD_USER, JOptionPane.OK_CANCEL_OPTION);
                if (!(Objects.equals(login, null)) && (!(Objects.equals(login, "")))) {
                    sendEventToController(Event.VIEW_ADD_USER_TO_DIALOG, login);
                }
            }
        });
    }

    private void addListenerOnComboChangeDialog() {
        jComboBoxDialogs.addActionListener(e -> {
            //только если произошло в результате нажатия на каку-либо кнопку
            //если программно поменяли, то событие не обрабатываем
            if (e.getModifiers() != 0) {
                sendEventToController(Event.VIEW_CHOOSE_DIALOG, jComboBoxDialogs.getSelectedItem());
            }
        });
    }

    /**
     * Настройка слушателей на события
     */
    private void buildingListeners() {
        addListenerOnSendMessage();
        addListenerOnEnter();
        addListenerOnCreateDialog();
        addListenerOnAddUserToDialog();
        addListenerOnComboChangeDialog();
    }

    /**
     * Отправляет контроллер произошедшее событие и контекст события
     *
     * @param event
     * @param context
     */
    private void sendEventToController(Event event, Object... context) {
        windowControllerChat.tryTakeEvent(event, context);
    }

    /**
     * Построение панели с информацией о входе пользователя
     */
    private void buildingSettingsPanel() {
        settingsPanel.setLayout(new FlowLayout());
        settingsPanel.setBackground(WindowTitles.COLOR_CHAT_PANEL);

        userField = new JTextField(null, null, 10);
        passwordField = new JPasswordField(null, null, 10);
        enterButton = new JButton(WindowTitles.TITLE_BUTTON_DO_ENTER);

        settingsPanel.add(new JLabel(WindowTitles.TITLE_СURRENT_USER));
        settingsPanel.add(userField);
        settingsPanel.add(new JLabel(WindowTitles.TITLE_BUTTON_PASSWORD));
        settingsPanel.add(passwordField);
        settingsPanel.add(enterButton);

        isNewUser = new JCheckBox(WindowTitles.TITLE_FLAG_NEW_USER);
        settingsPanel.add(isNewUser);
        WindowViewChat windowViewChat = this;
        isNewUser.addActionListener(e -> {
            if (isNewUser.isSelected()) {
                windowViewChat.enterButton.setText(WindowTitles.TITLE_BUTTON_DO_CREATE);
            } else {
                windowViewChat.enterButton.setText(WindowTitles.TITLE_BUTTON_DO_ENTER);
            }
        });
    }

    /**
     * Построение панели связанной с окном сообщений и текущим сообщением
     */
    private void buildingMainPanel() {
        mainPanel.setLayout(new BorderLayout());
        currentInfoLabel = new JLabel();
        currentInfoLabel.setHorizontalAlignment(JLabel.CENTER);
        currentInfoLabel.setFont(WindowTitles.FONT_TITLE_PANEL);
        mainPanel.add(currentInfoLabel, BorderLayout.NORTH);
        messagesTextPane = new JTextPane();
        messagesTextPane.setBackground(WindowTitles.COLOR_CHAT_MESSAGES);
        messagesTextPane.setEditable(false);
        JScrollPane scrollPane1 = new JScrollPane(messagesTextPane);
        mainPanel.add(scrollPane1);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        mainPanel.add(jPanel, BorderLayout.SOUTH);
        currentMessage = new JTextArea(4, 10);
        JScrollPane scrollPane2 = new JScrollPane(currentMessage);
        currentMessage.setBackground(WindowTitles.COLOR_CHAT_MESSAGE);
        jPanel.add(scrollPane2);
        sendMessageButton = new JButton(WindowTitles.TITLE_BUTTON_SEND_MESSAGE);
        jPanel.add(sendMessageButton);
        //почему-то не спозиционировалось именно справа
        sendMessageButton.setAlignmentX(1f);
    }

    /**
     * Построение панель списка пользователей диалога
     *
     * @param users
     */
    public void buildingUsersPanel(Set<UserInChat> users) {
        if (users != null) {
            List<UserInChat> tempUsers = new ArrayList<>(users);
            for (UserInChat userInChat : tempUsers) {
                UserLabel jLabel = new UserLabel(userInChat);
                dialogUserLabels.add(jLabel);
                usersPanel.add(jLabel);
            }
        }
        usersPanel.updateUI();
    }

    /**
     * Очистка панели пользователей диалога
     */
    public void cleanUsersPanel() {
        for (JLabel dialogUserLabel : dialogUserLabels) {
            usersPanel.remove(dialogUserLabel);
        }
        dialogUserLabels = new ArrayList<>();
    }

    /**
     * Построение панели с информацией о доступных диалогах
     *
     * @param dialogs
     */
    public void buildingDialogsPanel(Set<kvv.education.khasang.java1.chat.model.basic_entity.Dialog> dialogs) {
        if (dialogs != null) {
            List<kvv.education.khasang.java1.chat.model.basic_entity.Dialog> tempDialogs = new ArrayList<kvv.education.khasang.java1.chat.model.basic_entity.Dialog>(dialogs);
            buildingRadioButtons(tempDialogs);
            buildingCombobox(tempDialogs);
        }
    }

    /**
     * Построение комбо диалогов
     *
     * @param dialogs
     */
    private void buildingCombobox(List<kvv.education.khasang.java1.chat.model.basic_entity.Dialog> dialogs) {
        jComboBoxDialogs.removeAllItems();
        for (kvv.education.khasang.java1.chat.model.basic_entity.Dialog dialog : dialogs) {
            jComboBoxDialogs.addItem(dialog);
        }
        jComboBoxDialogs.setAlignmentX(0.1f);
        jComboBoxDialogs.setSelectedItem(null);
    }

    /**
     * Построение радиобатон диалогов
     *
     * @param dialogs
     */
    private void buildingRadioButtons(List<kvv.education.khasang.java1.chat.model.basic_entity.Dialog> dialogs) {
        buttonGroup = new ButtonGroup();
        for (kvv.education.khasang.java1.chat.model.basic_entity.Dialog dialog : dialogs) {
            DialogRadioButton jRadioButton = new DialogRadioButton();
            jRadioButton.setText(dialog.getName());
            jRadioButton.dialog = dialog;
            jRadioButton.setBackground(WindowTitles.COLOR_CHAT_PANEL);

            jRadioButton.addActionListener(e -> {
                //генерим событие только если нажатие на кнопку произошло не программно
                if (e.getModifiers() != 0) {
                    if (((JRadioButton) e.getSource()).isSelected()) {
                        sendEventToController(Event.VIEW_CHOOSE_DIALOG, ((DialogRadioButton) e.getSource()).dialog);
                    }
                }
            });

            buttonGroup.add(jRadioButton);
            dialogsPanel.add(jRadioButton);
            dialogRadioButtons.add(jRadioButton);
        }
    }

    /**
     * Очистка панели диалогов пользователей
     */
    public void cleanDialogsPanel() {
        jComboBoxDialogs.removeAllItems();
        Component[] components = dialogsPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JRadioButton) {
                dialogsPanel.remove(components[i]);
            }
        }
        dialogRadioButtons = new ArrayList<>();
    }

    /**
     * Вывод сообщения в окно сообщений беседы
     *
     * @param message
     * @param autor
     */
    public void addMessage(Message message, String autor) {
        final String separator = "\n";
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyledDocument document = messagesTextPane.getStyledDocument();
        try {
            StyleConstants.setBold(attributeSet, true);
            if (currentUser.getLogin().equals(autor)) {
                StyleConstants.setForeground(attributeSet, Color.BLUE);
            } else {
                StyleConstants.setForeground(attributeSet, Color.RED);
            }
            String temp = "(" + message.getDate() + ")" + " " + autor + ":" + separator;
            document.insertString(document.getLength(), temp, attributeSet);
            temp = message.getText() + separator + separator;
            StyleConstants.setBold(attributeSet, false);
            StyleConstants.setForeground(attributeSet, Color.BLACK);
            document.insertString(document.getLength(), temp, attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отработка полученных событий
     *
     * @param event
     * @param contextEvent
     */
    void processingEvent(Event event, Object... contextEvent) {
        switch (event) {
            case MODEL_USER_DO_EXIT:
                setCurrentUser(UNDEFINED_USER, null);
                break;
            case MODEL_USER_DO_ENTER:
                //контекст - текущий пользователь, множество доступных бесед
                setCurrentUser((User) contextEvent[0], (Set<kvv.education.khasang.java1.chat.model.basic_entity.Dialog>) contextEvent[1]);
                tabbedPane.setSelectedIndex(0);
                break;
            case MODEL_CHANGE_DIALOG:
                //контекст - текущая беседа, множество участников беседы
                setCurrentDialog((kvv.education.khasang.java1.chat.model.basic_entity.Dialog) contextEvent[0], (Set<UserInChat>) contextEvent[1]);
                break;
            case CONTROLLER_CMD_TO_VIEW_SHOW_MESSAGE:
                //контекст - отправленное сообщение, автор
                addMessage((Message) contextEvent[0], (String) contextEvent[1]);
                break;
            case MODEL_CREATE_DIALOG:
                //контекст - множество доступных диалогов, множество участников текущего диалога
                //перестроим панель доступных диалогов
                cleanDialogsPanel();
                buildingDialogsPanel((Set<kvv.education.khasang.java1.chat.model.basic_entity.Dialog>) contextEvent[0]);
                //панель перестроилась инфо о диалогах перестроилась, восстановим инфо о текущем диалоге
                setCurrentDialog(currentDialog, (Set<UserInChat>) contextEvent[1]);
                break;
            case MODEL_ADD_USER_TO_DIALOG:
                //контекст - множество участников текущего диалога
                cleanUsersPanel();
                buildingUsersPanel((Set<UserInChat>) contextEvent[0]);
                break;
            case CONTROLLER_CMD_TO_VIEW_CHANGE_USER_STATUS:
                //контекст - пользователь, у которого изменился статус, новый статус пользователя
                User user = (User) contextEvent[0];
                UserStatus newStatus = (UserStatus) contextEvent[1];
                changeStatus(user, newStatus);
                break;
            case MODEL_CREATED_EXCEPTION:
                //контекст -  сообщение про возникшую исключительную ситуацию
                showError((String) contextEvent[0]);
                break;
            case CONTROLLER_CMD_TO_VIEW_SHOW_MSG_WINDOW:
                //контекст - тип сообщения, заголовок, текст
                showMessage((int) contextEvent[0], (String) contextEvent[1], (String) contextEvent[2]);
                break;
            case CONTROLLER_UPDATED_LIST_MSG: {
                //нужно только очистить поле ввода текущего сообщения, т.к. в окно сообщений инфо попадает из модели через команды от контролера CONTROLLER_CMD_TO_VIEW_SHOW_MESSAGE
                cleanCurrentMessage();
                break;
            }
            default: {
                throw new IllegalArgumentException("Вью: неизвестная команда от контролера: " + event);
            }
        }
    }

    /**
     * Устанавливает текущего пользователя
     *
     * @param currentUser текущий пользователь
     * @param dialogs     диалоги доступные пользователю
     */
    public void setCurrentUser(User currentUser, Set<kvv.education.khasang.java1.chat.model.basic_entity.Dialog> dialogs) {
        this.currentUser = currentUser;
        cleanPassword();
        cleanErrorInfo();
        cleanDialogsPanel();
        if (currentUser != UNDEFINED_USER) {
            buildingDialogsPanel(dialogs);
        }

        setCurrentDialog(UNDEFINED_DIALOG, null);
    }

    /**
     * Устанавливает текущую беседу
     *
     * @param currentDialog текущая беседа
     * @param users         участники беседы
     */
    public void setCurrentDialog(Dialog currentDialog, Set<UserInChat> users) {
        stopListener();
        this.currentDialog = currentDialog;
        cleanUsersPanel();
        cleanCurrentMessage();
        cleanCurrentAreaMessages();
        cleanErrorInfo();

        if (currentDialog != UNDEFINED_DIALOG) {
            //если текущий диалог определен
            buildingUsersPanel(users);
            updateInfoAboutCurrentDialog();
            //запускаем слушателя обновлений в окне сообщений при необходимости
            if (isDoListenNewMessages()) {
                startListener();
            }
        }
        updateStatus();
    }

    public boolean isDoListenNewMessages() {
        return doListenNewMessages;
    }

    public void setDoListenNewMessages(boolean doListenNewMessages) {
        this.doListenNewMessages = doListenNewMessages;
    }

    /**
     * Останавливает поток опроса контроллера наличия новых сообщений в беседе
     */
    private void stopListener() {
        if ((newsListener != null) && (newsListener.isAlive())) {
            newsListener.interrupt();
        }
    }

    /**
     * Запускает поток опроса контроллера на предмет наличия новых сообщений в беседе
     */
    private void startListener() {
        WindowViewChat windowViewChat = this;
        newsListener = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(UPDATE_MSG_PERIOD);
                } catch (InterruptedException e) {
                    //прерывание приходит когда меняется диалог

                    //сработал catch => флаг прерывания сбросился, еще раз ставим
                    Thread.currentThread().interrupt();
                    //почему-то и так не работает - хоть isInterrupted = true, поток не останавливается
                    //System.err.println("внутри catch "+Thread.currentThread().isInterrupted()); //показывает true

                    // поэтому закончим поток просто разрушением цикла
                    break;
                }
                //System.err.println("вне саtch " +Thread.currentThread().isInterrupted()); //показывает true
                windowViewChat.sendEventToController(Event.VIEW_UPDATE_MSGS);
            }
        });

        // newsListener.run();
        // почему то не работает, запускаем по тому же пути, но в прежнем потоке?
        newsListener.start();
    }

    private void updateInfoAboutCurrentDialog() {
        //установить значение в радиобатон
        for (DialogRadioButton radioButton : dialogRadioButtons) {
            if (radioButton.dialog.equals(currentDialog)) {
                radioButton.setSelected(true);
            }
        }
        //установить значение в комбо
        jComboBoxDialogs.setSelectedItem(currentDialog);
    }

    /**
     * Обновляет отображение информации о текущем пользователе и беседе
     */
    private void updateStatus() {
        if (Objects.equals(currentUser, UNDEFINED_USER)) {
            showCurrentInfo(WindowTitles.STATUS_UNDEF_USER, WindowTitles.COLOR_MSG_STATUS_UNDEF_USER);
            showTitleWithSuffix(WindowTitles.TITLE_PANEL1 + ". " + WindowTitles.STATUS_UNDEF_USER);
            return;
        }
        if (Objects.equals(currentDialog, UNDEFINED_DIALOG)) {
            showCurrentInfo(WindowTitles.TITLE_СURRENT_USER + ": '" + currentUser.getLogin() + "'. " + WindowTitles.TITLE_UNDEF_DLG, WindowTitles.COLOR_MSG_STATUS_UNDEF_DLG);
            showTitleWithSuffix(WindowTitles.TITLE_PANEL1 + ". " + WindowTitles.TITLE_UNDEF_DLG);
            return;
        }
        showCurrentInfo(WindowTitles.TITLE_СURRENT_USER + ": '" + currentUser.getLogin() + "'.   " + WindowTitles.TITLE_CURRENT_DLG + ": '" + currentDialog.getName() + "'", WindowTitles.COLOR_MSG_STATUS_DLG_DEFINE);
        showTitleWithSuffix(WindowTitles.TITLE_PANEL1 + ". " + WindowTitles.TITLE_СURRENT_USER + ": '" + currentUser.getLogin() + "'.   " + WindowTitles.TITLE_CURRENT_DLG + ": '" + currentDialog.getName());
    }
}
