/* JS Document */

/******************************

 [Table of Contents]

 1. Vars and Inits
 2. Set Header
 3. Init Menu
 4. Init Home Slider
 5. Init Scrolling
 6. Init Isotope
 7. Init Testimonials Slider
 8. Init Input


 ******************************/
function get_book_item(id, title, year, author, publisher, rating = '') {
    html = "<div class=\"book item\">\n" +
        "                        <div class=\" book_image\">\n" +
        "                            <img src=\"/static/images/logo-IU.jpg\" alt=\"\">\n" +
        "                        </div>\n" +
        "                        <div class=\" book_content\">\n" +
        "                            <div class=\"book_title\">\n" +
        "                                <a href='/book/detail/" + id + "'>" + title +
        "                                </a>\n" +
        "                            </div>\n" +
        "                            <div class=\"book_publisher\">\n";
    if (rating != '') {
        html += "                                <span><i>" + rating + "</i></span> -\n";
    }
    html += "                                <a href=\"#\">" + year + "</a> -\n" +
        "                                <a href=\"#\">" + publisher + "</a>\n" +
        "                            </div>\n" +
        "                            <div class=\"book_author\">\n" +
        "                                <a href=\"#\">" + author + "</a> -\n" +
        "                            </div>\n" +
        "                        </div>\n" +
        "                    </div>";
    return html;
}

$(document).ready(function () {
    "use strict";

    /*

    1. Vars and Inits

    */

    var header = $('.header');
    var headerSocial = $('.header_social');
    var menu = $('.menu');
    var menuActive = false;
    var burger = $('.hamburger');

    setHeader();

    $(window).on('resize', function () {
        setHeader();

        setTimeout(function () {
            $(window).trigger('resize.px.parallax');
        }, 375);
    });

    $(document).on('scroll', function () {
        setHeader();
    });

    initMenu();
    initHomeSlider();
    initTestimonialsSlider();
    initScrolling();
    initInput();
    initSelection();

    /*

    2. Set Header

    */

    function setHeader() {
        if ($(window).scrollTop() > 127) {
            header.addClass('scrolled');
            headerSocial.addClass('scrolled');
        } else {
            header.removeClass('scrolled');
            headerSocial.removeClass('scrolled');
        }
    }

    /*

    3. Set Menu

    */

    function initMenu() {
        if ($('.menu').length) {
            var menu = $('.menu');
            if ($('.hamburger').length) {
                burger.on('click', function () {
                    if (menuActive) {
                        closeMenu();
                    } else {
                        openMenu();
                    }
                });
            }
        }
        if ($('.menu_close').length) {
            var close = $('.menu_close');
            close.on('click', function () {
                if (menuActive) {
                    closeMenu();
                }
            });
        }
    }

    function openMenu() {
        menu.addClass('active');
        menuActive = true;
    }

    function closeMenu() {
        menu.removeClass('active');
        menuActive = false;
    }

    /*

    4. Init Home Slider

    */

    function initHomeSlider() {
        if ($('.home_slider').length) {
            var homeSlider = $('.home_slider');
            homeSlider.owlCarousel(
                {
                    items: 1,
                    autoplay: false,
                    loop: true,
                    nav: false,
                    dots: false,
                    smartSpeed: 1200
                });
        }
    }

    /*

    5. Init Scrolling

    */

    function initScrolling() {
        if ($('.home_page_nav ul li a').length) {
            var links = $('.home_page_nav ul li a');
            links.each(function () {
                var ele = $(this);
                var target = ele.data('scroll-to');
                ele.on('click', function (e) {
                    e.preventDefault();
                    $(window).scrollTo(target, 1500, {offset: -90, easing: 'easeInOutQuart'});
                });
            });
        }
    }

    /*

    7. Init Testimonials Slider

    */

    function initTestimonialsSlider() {
        if ($('.testimonials_slider').length) {
            var testSlider = $('.testimonials_slider');
            testSlider.owlCarousel(
                {
                    animateOut: 'fadeOut',
                    animateIn: 'flipInX',
                    items: 1,
                    autoplay: true,
                    loop: true,
                    smartSpeed: 1200,
                    dots: false,
                    nav: false
                });
        }
    }

    /*

    8. Init Input

    */

    function initInput() {
        if ($('.newsletter_input').length) {
            var inpt = $('.newsletter_input');
            inpt.each(function () {
                var ele = $(this);
                var border = ele.next();

                ele.focus(function () {
                    border.css({'visibility': "visible", 'opacity': "1"});
                });
                ele.blur(function () {
                    border.css({'visibility': "hidden", 'opacity': "0"});
                });

                ele.on("mouseenter", function () {
                    border.css({'visibility': "visible", 'opacity': "1"});
                });

                ele.on("mouseleave", function () {
                    if (!ele.is(":focus")) {
                        border.css({'visibility': "hidden", 'opacity': "0"});
                    }
                });

            });
        }
    }

    function initSelection() {
        $('select').selectpicker();
    }
});