package jhu.group6.sounDJam.controllers;

import io.javalin.Context;

public class HealthCheckController {
    public static void healthCheck(Context ctx) {
        String ok = "ok!";
        ctx.result(ok);
        ctx.status(200);
    }
}
