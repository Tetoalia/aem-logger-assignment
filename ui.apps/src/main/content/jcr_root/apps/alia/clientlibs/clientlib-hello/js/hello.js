/**
 * hello.js — Part 1: AJAX call to path-registered servlet
 *
 * On DOMContentLoaded, calls /bin/alia/hello and injects the HTML response
 * into any element with class 'cmp-hello-target' on the page.
 */
(function () {
    "use strict";

    function loadHelloContent() {
        var targets = document.querySelectorAll(".cmp-hello-target");
        if (!targets || targets.length === 0) {
            return;
        }

        fetch("/bin/alia/hello", {
            method: "GET",
            headers: { "Accept": "text/html" }
        })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Servlet responded with status " + response.status);
            }
            return response.text();
        })
        .then(function (html) {
            targets.forEach(function (target) {
                target.innerHTML = html;
            });
        })
        .catch(function (err) {
            console.error("[clientlib-hello] Failed to load hello servlet:", err);
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", loadHelloContent);
    } else {
        loadHelloContent();
    }
}());
