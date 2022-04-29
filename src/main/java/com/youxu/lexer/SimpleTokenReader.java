package com.youxu.lexer;

import java.util.List;

public class SimpleTokenReader implements TokenReader{
    List<SimpleToken> tokens;
    int pos = 0;

    public SimpleTokenReader(List<SimpleToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public SimpleToken read() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return null;
    }

    @Override
    public SimpleToken peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return null;
    }

    @Override
    public void unread() {
        if (pos > 0) {
            pos--;
        }
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public void setPosition(int position) {
        if (position >=0 && position < tokens.size()){
            pos = position;
        }
    }
}
