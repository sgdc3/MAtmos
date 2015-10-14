package eu.ha3.matmos.gui.editor.condition;

import com.google.common.base.Optional;
import eu.ha3.matmos.MAtmos;
import eu.ha3.matmos.engine.condition.Checkable;
import eu.ha3.matmos.engine.condition.ConditionParser;
import eu.ha3.matmos.game.MCGame;
import eu.ha3.matmos.gui.editor.TextField;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * @author dags_ <dags@dags.me>
 */

public class ConditionField extends TextField
{
    private final MAtmos mAtmos;
    private final ConditionParser parser;

    private int opStart = -1;
    private String statement = "";
    private boolean updated = false;
    private Optional<Checkable> condition = Optional.absent();

    public ConditionField(MAtmos engine, ConditionParser conditionParser)
    {
        this(engine, conditionParser, "");
    }

    public ConditionField(MAtmos engine, ConditionParser conditionParser, String rule)
    {
        super();
        parser = conditionParser;
        mAtmos = engine;
        setText(rule);
    }

    @Override
    public void onKeyType(char c, int keyCode)
    {
        if (c == ' ')
        {
            return;
        }
        super.onKeyType(c, keyCode);
    }

    @Override
    public void append(char c)
    {
        if (opChar(c))
        {
            if (opStart < 0)
            {
                opStart = text.length();
            }
            if (getOperator().length() < 2)
            {
                super.append(c);
                updated = true;
            }
        }
        else if (validChar(c))
        {
            super.append(c);
            updated = true;
        }
    }

    @Override
    public void delete(boolean ctrl)
    {
        updated = true;
        if (ctrl && opStart > 0)
        {
            if (this.length() - getOperator().length() > opStart)
            {
                Rule r = new Rule(){ public boolean check(char c){ return c != '|' && !opChar(c); }};
                super.delete(r);
            }
            else
            {
                Rule r = new Rule(){ public boolean check(char c){ return opChar(c); }};
                super.delete(r);
                opStart = -1;
            }
        }
        else if (ctrl)
        {
            Rule r = new Rule(){ public boolean check(char c){ return c != '.'; }};
            super.delete(r);
        }
        else
        {
            if (this.length() - getOperator().length() < opStart)
            {
                opStart = -1;
            }
            super.deleteLast();
        }
    }

    public String getOperator()
    {
        if (opStart > 0)
        {
            char c1 = charAt(opStart);
            char c2 = charAt(opStart + 1);
            return opChar(c2) ? "" + c1 + c2 : "" + c1;
        }
        return "";
    }

    @Override
    public String getString()
    {
        if (updated)
        {
            updated = false;
            String op = getOperator();
            statement = super.getString().replace("|", " | ");
            if (op.length() > 0)
            {
                statement = statement.replace(op, " " + op + " ");
            }
            condition = parser.parse(statement);
        }
        return statement;
    }

    @Override
    public String getString(boolean showCursor)
    {
        return getString() + (showCursor ? "|" : "");
    }

    public Checkable get()
    {
        return condition.get();
    }

    public boolean valid()
    {
        updated = true;
        return condition.isPresent();
    }

    public void draw(boolean showCursor, int left, int top, int right)
    {
        if (active())
        {
            super.draw(showCursor, left, top, right, valid() ? 0x8CD156 : 0xD15E56);
        }
        else
        {
            super.draw(false, left, top, right, valid() ? 0xFFFFFFFF : 0xD15E56);
        }
        if (valid())
        {
            String data = get().getCurrentValue();
            int x = Minecraft.getMinecraft().fontRendererObj.getStringWidth("[" + data + "]");
            MCGame.drawString("[" + data + "]", right - x - 1, top, get().active() ? 0xFFFFFF : 0x999999);
        }
    }

    @Override
    protected List<String> tabComplete(String match)
    {
        return mAtmos.dataManager.findMatches(match);
    }

    private static boolean opChar(char c)
    {
        return c == '=' || c == '>' || c == '<' || c == '!';
    }

    private static boolean validChar(char c)
    {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-' || c == '|' || c == ':';
    }
}