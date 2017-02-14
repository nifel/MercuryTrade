package com.mercury.platform.shared;

import com.mercury.platform.shared.pojo.CurrencyMessage;
import com.mercury.platform.shared.pojo.ItemMessage;
import com.mercury.platform.shared.pojo.Message;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * need refactoring this shit
 */
public class MessageParser {
    public Message parse(String fullMessage){
        Message parsedMessage;
        String wNickname = StringUtils.substringBetween(fullMessage, "@From", ":");
        if(wNickname == null){
            wNickname = StringUtils.substringBetween(fullMessage, "@To", ":");
        }
        String message = StringUtils.substringAfter(fullMessage, wNickname + ":");
        wNickname = StringUtils.deleteWhitespace(wNickname);
        //todo regexp
        if(wNickname.contains(">")){
            wNickname = StringUtils.substringAfterLast(wNickname, ">");
        }
        Date msgDate = new Date(StringUtils.substring(fullMessage, 0, 20));
        String itemName = StringUtils.substringBetween(message, "to buy your ", " listed for");
        if(itemName == null){
            parsedMessage = getCurrencyMessage(message);
        }else {
            parsedMessage = getItemMessage(message);
        }
        parsedMessage.setWhisperNickname(wNickname);
        parsedMessage.setMessageDate(msgDate);
        parsedMessage.setSourceString(fullMessage);
        return parsedMessage;
    }
    private ItemMessage getItemMessage(String message){
        ItemMessage itemMessage = new ItemMessage();
        String itemName = StringUtils.substringBetween(message, "to buy your ", " listed for");
        String price = StringUtils.substringBetween(message, "listed for ", " in ");
        if(price == null){
            price = StringUtils.substringBetween(message, "for my ", " in ");
        }
        Double curCount = null;
        String currencyTitle = "";
        if(price != null) {
            curCount = Double.parseDouble(StringUtils.substringBefore(price," "));
            currencyTitle = StringUtils.substringAfter(price," ");
        }

        String offer = StringUtils.substringAfterLast(message, "in "); //todo
        String tabName = StringUtils.substringBetween(message, "(stash tab ", "; position:");
        if(tabName !=null ){
            offer = StringUtils.substringAfter(message, ")");
        }
        itemMessage.setItemName(itemName);
        itemMessage.setCurCount(curCount);
        itemMessage.setCurrency(currencyTitle);
        itemMessage.setTabName(tabName);
        itemMessage.setOffer(offer);
        return itemMessage;
    }
    private CurrencyMessage getCurrencyMessage(String message){
        CurrencyMessage currencyMessage = new CurrencyMessage();
        String currencyForSale = StringUtils.substringBetween(message, "to buy your ", " for my");
        Double currForSaleCount = null;
        String currForSaleTitle = "";
        if(currencyForSale != null) {
            currForSaleCount = Double.parseDouble(StringUtils.substringBefore(currencyForSale," "));
            currForSaleTitle = StringUtils.substringAfter(currencyForSale," ");
        }
        String price = StringUtils.substringBetween(message, "for my ", " in ");
        Double priceCount = null;
        String priceTitle = "";
        if(price != null) {
            priceCount = Double.parseDouble(StringUtils.substringBefore(price," "));
            priceTitle = StringUtils.substringAfter(price," ");
        }
        String offer = StringUtils.substringAfter(message, ".");
        currencyMessage.setCurrForSaleCount(currForSaleCount);
        currencyMessage.setCurrForSaleTitle(currForSaleTitle);
        currencyMessage.setCurCount(priceCount);
        currencyMessage.setCurrency(priceTitle);
        currencyMessage.setOffer(offer);
        return currencyMessage;
    }
}
