/**
 * loadmore.js — Part 2: Load More AJAX for customlist component
 *
 * On button click, calls <resourcePath>.loadmore.json?page=N
 * and appends new <li> cards to the existing list.
 * Hides the button when hasMore is false.
 */
(function () {
    "use strict";

    function initLoadMore(container) {
        var btn = container.querySelector(".cmp-customlist__loadmore-btn");
        if (!btn) return;

        var list = container.querySelector(".cmp-customlist__grid");
        if (!list) return;

        var resourcePath = container.getAttribute("data-resource-path");
        if (!resourcePath) return;

        btn.addEventListener("click", function () {
            var nextPage = parseInt(btn.getAttribute("data-next-page"), 10) || 2;

            btn.disabled = true;
            btn.textContent = "Loading…";

            fetch(resourcePath + ".loadmore.json?page=" + nextPage, {
                method: "GET",
                headers: { "Accept": "application/json" }
            })
            .then(function (res) {
                if (!res.ok) throw new Error("HTTP " + res.status);
                return res.json();
            })
            .then(function (data) {
                if (data.items && data.items.length > 0) {
                    data.items.forEach(function (item) {
                        var li = document.createElement("li");
                        li.className = "cmp-customlist__card";

                        var linkHref = item.url || (item.path + ".html");
                        var titleHtml = "<h3 class=\"cmp-customlist__title\">" +
                            "<a href=\"" + escapeHtml(linkHref) + "\">" +
                            escapeHtml(item.title || "") +
                            "</a></h3>";

                        var subTextHtml = "";
                        if (item.subText) {
                            subTextHtml = "<p class=\"cmp-customlist__subtext\">" +
                                item.subText + "</p>";
                        }

                        li.innerHTML = titleHtml + subTextHtml;
                        list.appendChild(li);
                    });
                }

                if (data.hasMore) {
                    btn.setAttribute("data-next-page", nextPage + 1);
                    btn.disabled = false;
                    btn.textContent = "Load More";
                } else {
                    // No more pages — hide the Load More container
                    var btnContainer = container.querySelector(".cmp-customlist__loadmore");
                    if (btnContainer) btnContainer.style.display = "none";
                }
            })
            .catch(function (err) {
                console.error("[clientlib-customlist] Load More error:", err);
                btn.disabled = false;
                btn.textContent = "Load More";
            });
        });
    }

    function escapeHtml(str) {
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function init() {
        var containers = document.querySelectorAll(".cmp-customlist[data-cmp-is='customlist']");
        containers.forEach(function (c) { initLoadMore(c); });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
}());
