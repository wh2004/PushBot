package org.eu.ioreo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.shiro.session.Session;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot;

public class PushBot extends TelegramLongPollingSessionBot {

    /***    
     * 欢迎使用UniOreo Telegram Push Bot
     * 本Bot用于推送消息到任意多频道或多群组的Telegram聊天窗口 
     * 频道推送前请给予管理员权限
     * 
     * By UniOreoX
     * 
     */

    private static List<String> checkerIDs = new ArrayList<>(); //配置管理员ID
    private static List<String> pushToIDs = new ArrayList<>(); //配置频道ID
    private static String botToken = ""; //配置botToken
    private static String botUsername = "UniChannelBot"; //配置botUsername

    public PushBot() {
        super();
        //add Checker ID
        checkerIDs.add("");
        //add PushTo ID
        pushToIDs.add("-1001526454034");
        pushToIDs.add("-1001743410731");
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update, Optional<Session> botSession) {
        Session session = botSession.get();
        if(update.hasCallbackQuery() && checkerIDs.contains(update.getCallbackQuery().getFrom().getId().toString()))
        {
            String callBackFrom = update.getCallbackQuery().getFrom().getId().toString();
            System.out.print("Callback");
            String callBackData = update.getCallbackQuery().getData();
            int messageID = update.getCallbackQuery().getMessage().getMessageId();
            switch(callBackData)
            {
                case "Pass":
                    try {
                        System.out.print("Pass");
                        SendMessage confirmAdmin = SendMessage.builder()
                        .chatId(callBackFrom)
                        .parseMode(ParseMode.HTML)
                        .text("成功通过投稿")
                        .disableNotification(true)
                        .replyToMessageId(messageID)
                        .build();
                        DeleteMessage deleteYMessage = DeleteMessage.builder()
                        .chatId(callBackFrom)
                        .messageId(messageID)
                        .build();
                        execute(confirmAdmin);
                        for(int i = 0; i < pushToIDs.size(); i++)
                        {
                            CopyMessage copyMessage = CopyMessage.builder()
                            .chatId(pushToIDs.get(i))
                            .parseMode(ParseMode.HTML)
                            .fromChatId(callBackFrom)
                            .messageId(messageID)
                            .replyMarkup(null)
                            .disableNotification(false)
                            .build();

                            execute(copyMessage);
                        }
                        execute(deleteYMessage);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    break;
                case "Reject":
                    System.out.print("Reject");
                    SendMessage rejectAdmin = SendMessage.builder()
                    .chatId(callBackFrom)
                    .parseMode(ParseMode.HTML)
                    .text("成功拒绝投稿")
                    .replyToMessageId(messageID)
                    .disableNotification(false)
                    .build();
                    DeleteMessage deleteNMessage = DeleteMessage.builder()
                    .chatId(callBackFrom)
                    .messageId(messageID)
                    .build();
                    try {
                        execute(rejectAdmin);
                        execute(deleteNMessage);
                    } catch (TelegramApiException e2) {
                        e2.printStackTrace();
                    }
                    break;
            }
        }
        if(update.hasMessage() && update.getMessage().hasText() && update.getMessage().isUserMessage()) 
        {
            if(session.getAttribute("进度")==null)
            {
                session.setAttribute("进度","起始");
            }
            System.out.print(session.getAttribute("进度"));
            String message = update.getMessage().getText();
            String username;
            if(update.getMessage().getFrom().getFirstName()==null)
            {
                username = update.getMessage().getFrom().getLastName();
            }
            else if(update.getMessage().getFrom().getLastName()==null)
            {
                username = update.getMessage().getFrom().getFirstName();
            }
            else
            {
                username = update.getMessage().getFrom().getFirstName()+" "+update.getMessage().getFrom().getLastName();
            }
            session.setAttribute("用户名", username);
            if(message.equals("/start")) {
                System.out.print("Command /start");
                SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("欢迎为 UniOreoX Channel 投稿\n本机器人支持使用<a href=\"https://core.telegram.org/bots/api#html-style\">HTML语法</a>投稿\n所以请尽量避免多次使用与HTML有关符号，如操作后机器人未回复，请检查投稿内容中的符号，并在特殊符号前加入反斜杠\\重试\n\n\n请现在发送您的投稿内容的标题（<b>仅标签</b>）\n投稿内容在审核过后将会被推送到 UniOreoX Channel 我们将注明您的名字")
                .disableNotification(false)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .build();
                session.setAttribute("进度", "标题");
                try {
                    execute(sendMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            else
            {
                System.out.print("Message "+message);
                String status = session.getAttribute("进度").toString();
                switch (status) {
                    case "标题":
                        SendMessage titleOKMessage = SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("标题：\n" + message+"\n\n接下来请发送您的投稿内容（<b>仅内容</b>）")
                        .parseMode(ParseMode.HTML)
                        .replyToMessageId(update.getMessage().getMessageId())
                        .build();
                        
                        session.setAttribute("标题", message);
                        session.setAttribute("进度", "内容");
                        try {
                            execute(titleOKMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "内容":
                        SendMessage contentOKMessage = SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("内容：\n" + message+"\n\n接下来请发送您的投稿标签 （<b>仅标签</b>）标签以空格分隔以井号#开头")
                        .parseMode(ParseMode.HTML)
                        .replyToMessageId(update.getMessage().getMessageId())
                        .build();
                        session.setAttribute("内容", message);
                        session.setAttribute("进度", "标签");
                        try {
                            execute(contentOKMessage);
                        } catch (TelegramApiException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case "标签":
                        SendMessage tagOKMessage = SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("您的投稿标签为：\n" + message+"\n\n接下来请您确认投稿内容是否正确")
                        .replyToMessageId(update.getMessage().getMessageId())
                        .build();

                        String replacedTags = message;
                        String[] forbiddenedChars = {"<",">","&","\\"};
                        for(int i = 0; i < forbiddenedChars.length; i++)
                        {
                            if(replacedTags.contains(forbiddenedChars[i]))
                            {
                                replacedTags = replacedTags.replace(forbiddenedChars[i], "\\"+forbiddenedChars[i]);
                            }
                        }
                        System.out.print(replacedTags);

                        session.setAttribute("标签", replacedTags);
                        session.setAttribute("进度", "确认");
                        
                        KeyboardRow keyboardRow = new KeyboardRow();
                        keyboardRow.add(KeyboardButton.builder().text("#确认").build());
                        keyboardRow.add(KeyboardButton.builder().text("#取消").build());
                        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboardRow(keyboardRow)
                        .oneTimeKeyboard(true)
                        .build();
                        StringBuilder contentAllStringBuilder = new StringBuilder();
                        contentAllStringBuilder
                        .append("「")
                        .append(session.getAttribute("标题"))
                        .append("」\n\n")
                        .append(session.getAttribute("内容"))
                        .append("\n\n")
                        .append("标签 ")
                        .append(session.getAttribute("标签"))
                        .append("\n")
                        .append("\n感谢 <a href=\"tg://user?id="+update.getMessage().getFrom().getId()+"\">"+username+"</a> 投稿");
                        
                        SendMessage confirmMessage = SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .parseMode(ParseMode.HTML)
                        .text(contentAllStringBuilder.toString())
                        .replyMarkup(replyKeyboardMarkup)
                        .build();
                        try {
                            execute(tagOKMessage);
                            execute(confirmMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "确认":
                        if(message.equals("#确认"))
                        {
                            System.out.print("提交投稿");

                            StringBuilder contentAllStringBuilderAdmin = new StringBuilder();
                            contentAllStringBuilderAdmin
                            .append("「")
                            .append(session.getAttribute("标题"))
                            .append("」\n\n")
                            .append(session.getAttribute("内容"))
                            .append("\n\n")
                            .append("标签 ")
                            .append(session.getAttribute("标签"))
                            .append("\n")
                            .append("\n感谢 <a href=\"tg://user?id="+update.getMessage().getFrom().getId()+"\">"+username+"</a> 投稿");
                            List<InlineKeyboardButton> buttonList = new ArrayList<>();
                            InlineKeyboardButton passButton = InlineKeyboardButton.builder().text("通过").callbackData("Pass").build();
                            InlineKeyboardButton rejectButton = InlineKeyboardButton.builder().text("拒绝").callbackData("Reject").build();
                            buttonList.add(passButton);
                            buttonList.add(rejectButton);

                            InlineKeyboardMarkup inlineKeyboardMarkupAdmin = InlineKeyboardMarkup.builder()
                            .keyboardRow(buttonList)
                            .build();

                            for(int i = 0; i < checkerIDs.size(); i++)
                            {
                                try {
                                    Thread.sleep(2 * 1000);
                                    SendMessage confirmMessageAdmin = SendMessage.builder()
                                    .chatId(checkerIDs.get(i))
                                    .replyMarkup(inlineKeyboardMarkupAdmin)
                                    .parseMode(ParseMode.HTML)
                                    .text(contentAllStringBuilderAdmin.toString())
                                    .disableNotification(true)
                                    .build();
                                    execute(confirmMessageAdmin);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                            SendMessage alertAdmin = SendMessage.builder()
                            .chatId(update.getMessage().getFrom().getId())
                            .text("成功投稿,审核通过后将自动推送到频道")
                            .disableNotification(false)
                            .build();
                            try {
                                execute(alertAdmin);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            session.setAttribute("进度", "起始");
                        }
                        else
                        {
                            session.setAttribute("进度", "起始");
                            SendMessage alertAdmin = SendMessage.builder()
                            .chatId(update.getMessage().getFrom().getId())
                            .text("成功取消投稿,管理员将不会收到您的投稿")
                            .disableNotification(false)
                            .build();
                            try {
                                execute(alertAdmin);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    
                        
                    case "起始":
                        SendMessage alertUser = SendMessage.builder()
                        .chatId(update.getMessage().getFrom().getId())
                        .text("您还没有创建投稿，请输入命令 /start 来开始投稿")
                        .disableNotification(false)
                        .build();
                        try {
                            execute(alertUser);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    
                }   
            
            }
        }
        
    }


   
}
