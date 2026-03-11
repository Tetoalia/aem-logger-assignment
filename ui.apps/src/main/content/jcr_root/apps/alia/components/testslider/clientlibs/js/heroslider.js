/**
 * Hero Slider — client-side behaviour
 *
 * Pure vanilla JS; no external libraries needed.
 * Initialises every .cmp-heroslider[data-cmp-is="heroslider"] on the page.
 */
(function () {
    "use strict";

    var SELECTOR = '[data-cmp-is="heroslider"]';

    /**
     * Initialise a single hero slider instance.
     * @param {HTMLElement} root - the .cmp-heroslider container
     */
    function initSlider(root) {
        var slides     = Array.prototype.slice.call(root.querySelectorAll(".cmp-heroslider__slide"));
        var dots       = Array.prototype.slice.call(root.querySelectorAll(".cmp-heroslider__dot"));
        var btnPrev    = root.querySelector(".cmp-heroslider__nav--prev");
        var btnNext    = root.querySelector(".cmp-heroslider__nav--next");
        var counterCur = root.querySelector(".cmp-heroslider__counter-current");

        if (!slides.length) { return; }

        var current    = 0;
        var total      = slides.length;
        var autoTimer  = null;
        var AUTO_DELAY = 5000; // ms between auto-advances

        // ---- helpers --------------------------------------------------------

        function goTo(index) {
            // Wrap around
            index = ((index % total) + total) % total;

            // Update slides
            slides[current].classList.remove("cmp-heroslider__slide--active");
            dots[current].classList.remove("cmp-heroslider__dot--active");
            dots[current].setAttribute("aria-selected", "false");

            current = index;

            slides[current].classList.add("cmp-heroslider__slide--active");
            dots[current].classList.add("cmp-heroslider__dot--active");
            dots[current].setAttribute("aria-selected", "true");

            if (counterCur) {
                counterCur.textContent = current + 1;
            }
        }

        function startAuto() {
            clearInterval(autoTimer);
            autoTimer = setInterval(function () { goTo(current + 1); }, AUTO_DELAY);
        }

        function resetAuto() {
            clearInterval(autoTimer);
            startAuto();
        }

        // ---- event listeners ------------------------------------------------

        if (btnPrev) {
            btnPrev.addEventListener("click", function () {
                goTo(current - 1);
                resetAuto();
            });
        }

        if (btnNext) {
            btnNext.addEventListener("click", function () {
                goTo(current + 1);
                resetAuto();
            });
        }

        dots.forEach(function (dot, idx) {
            dot.addEventListener("click", function () {
                goTo(idx);
                resetAuto();
            });
        });

        // Keyboard navigation (left/right arrow keys) when focus is inside
        root.addEventListener("keydown", function (e) {
            if (e.key === "ArrowLeft")  { goTo(current - 1); resetAuto(); }
            if (e.key === "ArrowRight") { goTo(current + 1); resetAuto(); }
        });

        // Pause on hover / focus
        root.addEventListener("mouseenter", function () { clearInterval(autoTimer); });
        root.addEventListener("mouseleave", startAuto);
        root.addEventListener("focusin",    function () { clearInterval(autoTimer); });
        root.addEventListener("focusout",   startAuto);

        // Touch / swipe support
        var touchStartX = 0;
        root.addEventListener("touchstart", function (e) {
            touchStartX = e.changedTouches[0].clientX;
        }, { passive: true });

        root.addEventListener("touchend", function (e) {
            var delta = e.changedTouches[0].clientX - touchStartX;
            if (Math.abs(delta) > 50) {
                goTo(delta < 0 ? current + 1 : current - 1);
                resetAuto();
            }
        }, { passive: true });

        // ---- kick-off -------------------------------------------------------
        startAuto();
    }

    // ---- document ready -----------------------------------------------------

    function onReady() {
        var sliders = Array.prototype.slice.call(document.querySelectorAll(SELECTOR));
        sliders.forEach(initSlider);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", onReady);
    } else {
        onReady();
    }

    // AEM Authoring: re-initialise when the page model is refreshed
    if (window.Granite && window.Granite.author) {
        document.addEventListener("cq-editor-loaded", onReady);
    }
}());
