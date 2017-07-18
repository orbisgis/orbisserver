/*
 * OrbisServer is an OSGI web application to expose OGC services.
 *
 * OrbisServer is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisServer is distributed under LGPL 3 license.
 *
 * Copyright (C) 2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisServer is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisServer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * OrbisServer. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */


// <![CDATA[
$('#navbar').affix({
    offset: {
        top: $('header').height()
    }
});

$('#Mymodal').on('shown.bs.modal', function () {$('#sign in').focus()})

function process(){
    $.ajax({ type: "GET",
        data: {
            "token": readCookie("token")
        },
        url: "http://localhost:8080/process/leftNavContent",
        async: false,
        success : function(text)
        {
            $('#left-nav').addClass('slide-in');
            $('#main-body').css("margin-left", "510px");
            $('#content').addClass('col-xs-12 col-sm-11 col-sm-pull-0');
            $( "#left-nav-content" ).html(String(text));
        },
        error : function(text)
        {
            $('#left-nav').addClass('slide-in');
            $('#main-body').css("margin-left", "510px");
            $( "#left-nav-content" ).html(String("Error"));
        }
    });
}

function showProcess(id){
    $.ajax({ type: "GET",
        url: "http://localhost:8080/describeProcess",
        data: {
            "id": id,
            "token": readCookie("token")
        },
        async: false,
        success : function(text)
        {
            $( "#content" ).html(String(text));
        },
        error : function(text)
        {
            $( "#content" ).html(String(text));
        }
    });
}

function showUser(){
    $.ajax({
        type: "GET",
        data: {
            "token": readCookie("token")
        },
        url: "http://localhost:8080/user",
        async: false,
        success : function(text)
        {
            $( "#user_ul" ).html(String(text));
        },
        error : function(text)
        {
            $( "#user_ul" ).html(String("Error"));
        }
    });
}

function signIn(){
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/signIn",
        async: false,
        success : function(text)
        {
            $( "#content" ).html(String(text));
        },
        error : function(text)
        {
            $( "#content" ).html(String("Error"));
        }
    });
}

//** leftnav Script */
function jobs(){
        $.ajax({ type: "GET",
            url: "http://localhost:8080/jobs",
            data: {
                "token": readCookie("token")
            },
            async: false,
            success : function(text)
            {
                $( "#list" ).html("");
                $( "#content" ).html(String(text));
            },
            error : function(text)
            {
                $( "#content" ).html(String(text));
            }
        });
    }

    function importData(){
        $.ajax({ type: "GET",
            url: "http://localhost:8080/process/import",
            data: {
                "token": readCookie("token")
            },
            async: false,
            success : function(text)
            {
                if($('#dropdown-lvl3').attr('class')=="panel-collapse collapse in"){
                    $('#dropdown-lvl3').removeClass('in');
                }
                if($('#dropdown-lvl1').attr('class')=="panel-collapse collapse in"){
                    $('#dropdown-lvl1').removeClass('in');
                }
                $('#left-nav').addClass('slide-in');
                $( '#import-list' ).html(String(text));
            },
            error : function(text)
            {
                $( '#content' ).html(String(text));
            }
        });
    }

    function exportData(){
        $.ajax({ type: "GET",
            url: "http://localhost:8080/process/export",
            data: {
                "token": readCookie("token")
            },
            async: false,
            success : function(text)
            {
                if($('#dropdown-lvl2').attr('class')=="panel-collapse collapse in"){
                    $('#dropdown-lvl2').removeClass('in');
                }
                if($('#dropdown-lvl1').attr('class')=="panel-collapse collapse in"){
                    $('#dropdown-lvl1').removeClass('in');
                }
                $('#left-nav').addClass('slide-in');
                $( '#export-list').html(String(text));
            },
            error : function(text)
            {
                $( '#export-list'  ).html(String(text));
            }
        });
    }

    function listProcess(){
        $.ajax({ type: "GET",
            data: {
                "token": readCookie("token")
            },
            url: "http://localhost:8080/process/processList",
            async: false,
            success : function(text)
            {
                if($('#dropdown-lvl2').attr('class')=="panel-collapse collapse in"){
                    $('#dropdown-lvl2').removeClass('in');
                }
                if($('#dropdown-lvl3').attr('class')=="panel-collapse collapse in"){
                    $('#dropdown-lvl3').removeClass('in');
                }
                $('#left-nav').addClass('slide-in');
                $( '#process-list' ).html(String(text));
            },
            error : function(text)
            {
                $( '#process-list' ).html(String("Error"));
            }
        });
    }


/** Login modal scripts */
$(function() {
    $("#login-form").on("submit", function(e) {
        e.preventDefault();
        $.ajax({
            url: $('#login-form')[0].action,
            type: 'POST',
            data: $(this).serialize(),
            beforeSend: function() {
                $("#login-refresh").addClass('gly-spin');
                $("#login-refresh").addClass('glyphicon-refresh');
                $('#login_btn').prop("disabled", true);
            },
            success: function(data) {
                writeCookie("token",data);
                $('#loginModal').modal('hide');
                $("#login-refresh").removeClass('gly-spin');
                $("#login-refresh").removeClass('glyphicon-refresh');
                $("#login_btn").html('Login');
                $('#login_btn').prop("disabled", false);
                $('#text-login-msg').html("Type your username and password");
                $('#login-modal-body').removeClass('has-error')
                showUser();
            },
            error: function(data) {
                $("#login-refresh").removeClass('gly-spin');
                $("#login-refresh").removeClass('glyphicon-refresh');
                $("#login_btn").html('Login');
                $('#login_btn').prop("disabled", false);
                $('#login-modal-body').addClass('has-error')
                $('#text-login-msg').html(data.responseText);
            }
        });
    });
});

function register(){
    if(~$('#login_btn').html().indexOf('Register')){
        $('#login_btn').html('Login');
        $('#login_register_btn').html('Register');
        $('#login-form')[0].action='/login';
    }
    else if(~$('#login_btn').html().indexOf('Login')){
        $('#login_btn').html('Register');
        $('#login_register_btn').html('Login');
        $('#login-form')[0].action='/register';
    }
}

function pwdLost(){
    $('#login-footer-text').html('Please contact your administrator');
    setTimeout(hideLoginFooterText, 5000);
}

function hideLoginFooterText(){
    $('#login-footer-text').html('');
}

/** cookies functions */
function writeCookie(name, value) {
    document.cookie = name + "=" + value;
}

function readCookie(name) {
    var i, cookie, cookies, nameEq = name + "=";
    cookies = document.cookie.split(';');
    for(i=0;i<cookies.length;i++) {
        cookie = cookies[i];
        while (cookie.charAt(0)==' ') {
            cookie = cookie.substring(1,cookie.length);
        }
        if (cookie.indexOf(nameEq) == 0) {
            return cookie.substring(nameEq.length,cookie.length);
        }
    }
    return '';
}

/** User scripts */
function user_settings(){
    $.ajax({
        type: "GET",
        data: {
            "token": readCookie("token")
        },
        url: "/user/settings",
        async: false,
        success : function(text)
        {
            $( "#content" ).html(String(text));
        },
        error : function(text)
        {
            $( "#content" ).html(text.responseText);
        }
    });
}

function log_out(){
    $.ajax({
        type: "GET",
        data: {
            "token": readCookie("token")
        },
        url: "http://localhost:8080/user/logOut",
        async: false,
        success : function(text)
        {
            $( "#user_ul" ).html(String(text));
            writeCookie("token", "");
            showUser();
        },
        error : function(text)
        {
            $( "#user_ul" ).html(String("Error"));
            showUser();
        }
    });
}

// ]]>