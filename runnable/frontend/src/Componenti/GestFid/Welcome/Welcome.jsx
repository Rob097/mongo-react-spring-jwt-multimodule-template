import React, { Component } from "react";
import { Link } from 'react-router-dom';
import "./Welcome.css";

//Classe per la pagina di benvenuto dell'utente subito dopo il login
export default class WelcomeComponent extends Component {

    render() {
        return (

            <div className="GestFidApp">
                <section id="welcomeSection">
                    <h1>Benvenuti in GestFid</h1>
                    <p>Saluti <b>{this.props.match.params.userId}</b></p>
                    <Link to="/clienti" type="button" className="btn btn-primary" >Vai ai clienti disponibili</Link>
                </section>                
            </div>

        );
    }

}