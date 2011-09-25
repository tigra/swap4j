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

    public Locator push(UUID newId, String additionalPathComponent) {
        Locator locator = stack.peek();
        locator = new Locator(newId, locator.getPath() + additionalPathComponent);
        stack.push(locator);
        return locator;
    }



    public void addRoot(UUID id) {
        stack.push(new Locator(id, "."));
    }

    public Locator peek() {
        return stack.peek();
    }

    @Override
    public String toString() {
        return "ObjectContext{stack=" + dump(stack) + '}';
    }

    private String dump(Stack<Locator> stack) {
        StringBuilder builder = new StringBuilder("[");
        for (Locator l: stack) {
            builder.append(l).append(",");
        }
        builder.append("]");
        return builder.toString();
    }
}