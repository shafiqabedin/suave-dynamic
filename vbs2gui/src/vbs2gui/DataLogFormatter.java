/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author owens
 */
public class DataLogFormatter extends Formatter {

    private static final MessageFormat messageFormat = new MessageFormat("{0}[{1}|{2}|{3}]: {4} \n");

    public DataLogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord record) {
        Object[] arguments = new Object[6];
        arguments[0] = record.getLoggerName();
        arguments[1] = record.getLevel();
        arguments[2] = Thread.currentThread().getName();
        arguments[3] = Long.toString(record.getMillis());
        arguments[4] = record.getMessage();
        return messageFormat.format(arguments);
    }
}
