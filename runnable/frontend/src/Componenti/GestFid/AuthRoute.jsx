import React, { Component, useState } from "react";
import ReactDOM from 'react-dom';
import createReactClass from "create-react-class";
import AuthenticationService from "./Services/authService.js";
import { Redirect, Route, Link } from "react-router-dom"
import jwt from "jsonwebtoken"
import axios from 'axios'
import { Alert, Button } from 'react-bootstrap';
import AlertsExpired from "./Login/AlertsExpired"

//Tempo che si vuole lasciare all'utente per rinnovare il token
const time = 15 * 60 * 1000;

//Inizializzo variabili globali di questo file
let token = null;
let exp = null;
let now = null;

//Questa classe viene utilizzata nel ROUTING alle diverse pagine dell'app. Viene quindi chiamata quando si vuole aprire tutte le pagine che sono indirizzate tramite questa classe.
//Visualizzi il suo utilizzo in GestFid.jsx
export default class AuthRoute extends Component {

	//Il costruttore viene eseguito al primo utilizzo della classe e avvia l'intercettore.
	constructor() {
		super();
		this.setupAxiosInterceptors();
	}

	//Metodo eseguito ogni volta alla fine del caricamento della classe.
	//Imposto correttamente le variabili globali e poi avvio il metodo loopIfExpired.
	componentDidMount = () => {
		try {
			this.setVariable();
			this.loopIfExpired();
		} catch (e) {
			console.log(e);
		}
	}

	//Metyodo per impostare correttamente le variabili globali con i valori più recenti.
	setVariable = () => {
		//Essendo che questo metodo viene chiamato anche nel componentDidMount, il quale è il primo metodo che parte, bisogna controllare che il token non sia null.
		//Il controllo principale viene comunque fatto nel metodo render.
		if(sessionStorage.getItem('token') != null){
			token = sessionStorage.getItem('token');
			exp = new Date(jwt.decode(token).exp*1000).getTime(); //.getTime mi fornisce il valore timestamp unix di una data.
			now = new Date().getTime();
		}
	}

	//Questo metodo serve per controllare automaticamente ogni periodo di tempo uguale alla costante globale time se il token è prossimo alla scadenza.
	//Se lo è, visualizzo una finestra di alert (AlertsExpired.jsx) con la possibilità di aggiornare il token o di tornare al login con anche il countdown.
	loopIfExpired = () => {

		setInterval(() => {

			//Aggiorno le varibili globali essendo dentro ad una funzione ricorsiva.
			this.setVariable();

			//Controllo se il token è prossimo alla scadenza
			if (this.checkIfExpired()) {

			//Aggiungo al body la finestra di alert
				var Hello = createReactClass({
					render: function () {
						return <AlertsExpired obj={this} show={true} diff={(exp-now)} time={time}/>;
					}
				});
				var temp = document.createElement("div");
				// render
				ReactDOM.render(<Hello />, temp);
				var container = document.querySelector("body");
				if(container.querySelector("#alertExp") === null || container.querySelector("#alertExp") === undefined){
					container.appendChild(temp, document.querySelector("body"));
				}
			//Fine di aggiunta al body della finestra di alert

			}
			//In ogni caso richiamo la funzione che aspetterà time tempo prima di eseguirsi nuovamente.
			this.loopIfExpired();
		}, time);
	}

	//Metodo per controlalre se il token è prossimo alla scadenza secondo la costante globale time
	checkIfExpired = () => {
		if (sessionStorage.getItem('token') !== null) {
			
			//Se la data exp è nel futuro rispetto ad adesso e se exp meno now è minore o uguale al periodo di allerta (time = 15 minuti) ritorno true
			if (now < exp && (exp-now) <= time) {
				return true;
			} else if (new Date() > exp) {//Se il token è già scaduto ritorno null
				return null;
			} else { //Se il token non è ne già scaduto ne è prossimo alla scadenza ritorno false
				return false;
			}
		}			
		console.log("TOKEN IS NULL");
		return null; //Se il token non viene trovato ritorno null
	}

	//Questo è un Intercettore (Interceptors).
	//Servono per svolgere un operazione prima che si invia una richiesta alle webapi
	setupAxiosInterceptors() {
		let token = 'Bearer ' + sessionStorage.getItem('token');
		//Questo è il metodo per usare l'intercettore.
		axios.interceptors.request.use(
			//Creo una nuova configurazione
			(config) => {

				//Devo aggiornare la variabile token anche qui altrimenti ritorna valori non aggiornati se non si aggiorna la pagina.
				token = 'Bearer ' + sessionStorage.getItem('token');
				if (AuthenticationService.isLogged()) {
					config.headers.authorization = token
				}
				return config;
			},
			(err) => {
				return Promise.reject(err);
			}
		)

	}

	render() {

		//Mi ricavo i ruoli dell'utente loggato
		let ruoli = "";
		if (sessionStorage.getItem('token') !== null)
			ruoli = jwt.decode(sessionStorage.getItem('token')).roles;

		//Aggiorno le variabili globali
		this.setVariable();

		//Controllo che l'utente sia loggato e che iltoken non sia già scaduto. Altrimenti reindirizzo alla pagina di login
		if (AuthenticationService.isLogged() && this.checkIfExpired() !== null) {

			//myRole è in realtà il ruolo che è necessario avere per poter accedere ad una determinata pagina e viene passato nelle props in GestFid.jsx
			let myRole = this.props.role;

			//Variabile di controllo per verificare che l'utnete abbia il ruolo necessario
			let check = false;

			//Verifico che l'utente abbia il ruolo necessario
			ruoli.forEach(e => {
				if (e.authority !== null && e.authority === myRole) {
					check = true;
				}
			})


			//Se l'utente non ha il ruolo necessario ad accedere alla pagina lo rimando alla pagina forbidden
			if (check) {				

				//Se il token sta per scadere visualizzo la finestra di alert e renderizzo la pagina richiesta.
				if (this.checkIfExpired()) {
					return (
						<>
							<AlertsExpired obj={this} show={true} diff={(exp-now)} time={time}/>
							<Route {...this.props}></Route>
						</>
					)
				} else { //Altrimenti renderizzo solamente la pagina richiesta
					return (
						<Route {...this.props}></Route>
					)
				}
			} else {
				return <Redirect to="/forbidden" />
			}

		} else {
			return <Redirect to="/login" />
		}

	}

}