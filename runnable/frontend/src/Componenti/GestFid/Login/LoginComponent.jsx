import React, { Component } from "react";
import "./LoginComponent.css";
import AuthenticationService from '../Services/authService.js';

//Classe per la pagina di login
export default class LoginComponent extends Component {

    state = {
        userId: 'Roberto',
        password: '123Stella',
        isLogged: false,
        noLogged: false
    }

    //Form di login
    render() {
        return (

            <div>
                <p>Accedi all'App GestFid</p>
                <section id="logInSection">
                    <form className="form-signin" onSubmit={this.Login}>
                        <img className="mb-4" src="https://getbootstrap.com/docs/4.0/assets/brand/bootstrap-solid.svg" alt="" width="72" height="72" />
                        <h1 className="h3 mb-3 font-weight-normal">Please sign in</h1>

                        <label htmlFor="userId" className="sr-only">Email address</label>
                        <input type="text" id="userId" name="userId" className="form-control" defaultValue={this.state.userId} onChange={this.GestMod} required autoFocus />

                        <label htmlFor="password" className="sr-only">Password</label>
                        <input type="password" id="password" name="password" className="form-control" defaultValue={this.state.password} onChange={this.GestMod} required />

                        <div className="checkbox mb-3">
                            <label>
                                <input type="checkbox" value="remember-me" /> Remember me
                            </label>
                        </div>
                        <button className="btn btn-lg btn-primary btn-block" type="submit">Accedi</button>
                        <p className="mt-5 mb-3 text-muted">&copy; 2017-2018</p>
                    </form>
                </section>                
            </div>

        );
    }

    //Metodo per il login.
    Login = (event) => {
        event.preventDefault(); //Importante percè essendo il login una form, evita che premendo invio la pagina si refreshi.

        //Chiamo la funzione che contatta l'api backend per il login e la generazione del token JWT
        AuthenticationService.JWTAuthServer(this.state.userId,this.state.password)
        .then((response) => {
            AuthenticationService.saveUserInfo(this.state.userId,response.data.accessToken); //Salvo le info in sessione
            this.props.history.push(`/welcome/${this.state.userId}`); //Reindirizzo nella pagina di welcome
        })
        .catch(() => {
            //Resetto le variabili dello state se trovo un problema
            this.setState({
                isLogged: false,
                noLogged: true  
            })
        })
    }

    //Metodo che aggiorna le variabili dello state quando vengono modificati i dati all'interno della form
    GestMod = (event) => {
        this.setState({
            [event.target.name]: event.target.value
        })
    }

}