package com.github.illarion.swap4j.store.scan;

import java.util.Stack;
import java.util.UUID;

public class ObjectContext {
    Stack<Locator> stack = new Stack<Locator>();

    void updateId(UUID id) {
        stack.push(new Locator(id, stack.peek().getPath()));
    }

    void pop() {
        stack.pop();
    }

    Locator push(String name) {
        Locator locator = stack.peek();
        Locator newLocator = new Locator(locator.getId(), locator.getPath() + "/" + name);
        stack.push(newLocator);
        return newLocator;
    }

    public Locator push(String name, UUID newId) {
        Locator locator = stack.peek();
        locator = new Locator(newId, locator.getPath() + "/" + name);
        stack.push(locator);
        return locator;
    }


    public void addRoot() {
        stack.push(new Locator(null, "."));
    }

    public Locator peek() {
        return stack.peek();
    }
}