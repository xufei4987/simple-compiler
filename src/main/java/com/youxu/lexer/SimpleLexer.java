package com.youxu.lexer;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

import static com.youxu.lexer.DfaState.*;

/**
 * 简单的词法分析器
 */
public class SimpleLexer {

    public static void main(String[] args) {
        SimpleLexer lexer = new SimpleLexer();

        String script = "int age = 45;";
        System.out.println("parse :" + script);
        SimpleTokenReader tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        script = "int age == 45;";
        System.out.println("parse :" + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        script = "intA == 45;";
        System.out.println("parse :" + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        script = "2 + 3 * 5;";
        System.out.println("parse :" + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);
    }

    private StringBuffer tokenText;  //临时保存token的文本
    private List<SimpleToken> tokens;  //保存解析出来的Token
    private SimpleToken token;  //当前正在解析的Token

    //是否是字母
    private boolean isAlpha(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    //是否是数字
    private boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    //是否是空白字符
    private boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    private DfaState initToken(char ch) {
        if (tokenText.length() > 0) {
            token.setText(tokenText.toString());
            tokens.add(token);

            tokenText = new StringBuffer();
            token = new SimpleToken();
        }
        DfaState newState;
        if (isAlpha(ch)) {
            if (ch == 'i') {
                newState = Id_int1;
            } else {
                newState = Id;
            }
            token.setTokenType(TokenType.Identifier);
            tokenText.append(ch);
        } else if (ch == '>') {
            newState = GT;
            token.setTokenType(TokenType.GT);
            tokenText.append(ch);
        } else if (isDigit(ch)) {
            newState = IntLiteral;
            token.setTokenType(TokenType.IntLiteral);
            tokenText.append(ch);
        } else if (ch == '=') {
            newState = Assignment;
            token.setTokenType(TokenType.Assignment);
            tokenText.append(ch);
        } else if (ch == '+') {
            newState = Plus;
            token.setTokenType(TokenType.Plus);
            tokenText.append(ch);
        } else if (ch == '-') {
            newState = Minus;
            token.setTokenType(TokenType.Minus);
            tokenText.append(ch);
        } else if (ch == '*') {
            newState = Star;
            token.setTokenType(TokenType.Star);
            tokenText.append(ch);
        } else if (ch == '/') {
            newState = Slash;
            token.setTokenType(TokenType.Slash);
            tokenText.append(ch);
        } else if (ch == ';') {
            newState = SemiColon;
            token.setTokenType(TokenType.SemiColon);
            tokenText.append(ch);
        } else {
            newState = Initial; // skip all unknown patterns
        }
        return newState;
    }

    public SimpleTokenReader tokenize(String code) {
        tokens = new ArrayList<>();
        tokenText = new StringBuffer();
        token = new SimpleToken();
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        int ich = 0;
        char ch = 0;
        DfaState state = Initial;
        try {
            while ((ich = reader.read()) != -1) {
                ch = (char) ich;
                switch (state) {
                    case Initial:
                    case GE:
                    case EQ:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                        state = initToken(ch);
                        break;
                    case GT:
                        if (ch == '=') {
                            state = GE;
                            token.setTokenType(TokenType.GE);
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id:
                        if (isDigit(ch) || isAlpha(ch)) {
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {
                            state = Id_int2;
                            tokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            state = Id; //切换回Id状态
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = Id_int3;
                            tokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            state = Id; //切换回Id状态
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBlank(ch)) {
                            token.setTokenType(TokenType.Int);
                            state = initToken(ch);
                        } else {
                            state = Id; //切换回Id状态
                            tokenText.append(ch);
                        }
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Assignment:
                        if (ch == '=') {
                            state = EQ;
                            token.setTokenType(TokenType.EQ);
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (tokenText.length() > 0) {
            token.setText(tokenText.toString());
            tokens.add(token);
        }
        return new SimpleTokenReader(tokens);
    }

    /**
     * 打印所有的Token
     *
     * @param tokenReader
     */
    public static void dump(SimpleTokenReader tokenReader) {
        System.out.println("text\ttype");
        SimpleToken token;
        while ((token = tokenReader.read()) != null) {
            System.out.println(token.getText() + "\t\t" + token.getTokenType());
        }
    }
}
