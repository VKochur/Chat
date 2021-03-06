package kvv.education.khasang.java1.chat.views.gui;

/**
 * События возникающие во вьюхе, в моделе, в контролере. или команды контролера /контролеру
 */
public enum Event {
    VIEW_DO_ENTER_USER,
    VIEW_CHOOSE_DIALOG,
    VIEW_SEND_MESSAGE,
    VIEW_CREATE_DIALOG,
    VIEW_CREATE_NEW_USER,
    VIEW_ADD_USER_TO_DIALOG,
    VIEW_UPDATE_MSGS,

    MODEL_USER_DO_EXIT,
    MODEL_USER_DO_ENTER,
    MODEL_CHANGE_DIALOG,
    MODEL_CREATE_DIALOG,
    MODEL_ADD_USER_TO_DIALOG,
    MODEL_CREATED_EXCEPTION,

    CONTROLLER_CMD_TO_VIEW_CHANGE_USER_STATUS,
    CONTROLLER_CMD_TO_VIEW_SHOW_MESSAGE,
    CONTROLLER_CMD_TO_VIEW_SHOW_MSG_WINDOW,
    CONTROLLER_UPDATED_LIST_MSG,
    TO_CONTROLLER_CMD_STOP_PROCESS
}
