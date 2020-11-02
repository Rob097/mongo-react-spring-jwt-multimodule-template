import React, { Component } from "react";
import "./LogoutComponent.css";

//Classe per la funzionalità di logout
export default class LogoutComponent extends Component {
    render() {
        this.clearUserInfo();
        this.props.history.push(`/login`); //Reindirizzo nella pagina di login
        return null;
    }

    clearUserInfo = () => {
        sessionStorage.removeItem("user");
        sessionStorage.removeItem("token");
    }
}