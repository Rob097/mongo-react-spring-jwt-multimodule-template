import axios from "axios";

//Funzionalità varie per quanto riguarda il login, l'autenticazione e la comunicazione con le API riguardanti gli utenti.
class AuthenticationService {

    //Servizio per controllare la user e la password nel login

    state = {
        Server: "http://localhost:8080"
    }

    createBasicAuthHeader = (username,password) => 'Basic ' + window.btoa(username + ":" + password);

    createJwtAuthToken = (token) => 'Bearer ' + token;

    authUser = (username, password) => {
        return axios.get(`${this.state.Server}${this.state.BaseURL}/auth`,
            {
                headers: {authorization: this.createBasicAuthHeader(username,password)}
            }
        );
    }

    refreshToken = () => {
        return axios.get(`${this.state.Server}/api/auth/refresh-token`);
    }

    //Con l'autenticazione JWT i parametri non vengono passati con un header ma attraverso il body
    JWTAuthServer = (username,password) => {
        return axios.post(`${this.state.Server}/api/auth/signin`,
            {
                username,
                password
            }
        );
    }

    saveUserInfo = (username,token) => {
        sessionStorage.setItem("user", username);
        sessionStorage.setItem("token", token);

        //Qui non serve più. è stato spostato nell'AuthRoute.jsx
        //Dico all'intercettore quando deve essere utilizzato
        //this.setupAxiosInterceptors(this.createJwtAuthToken(token));

    }

    clearUserInfo = () => {
        sessionStorage.removeItem("user");
        sessionStorage.removeItem("token");
    }

    getUSerInfo = () => sessionStorage.getItem("user");

    isLogged = () => {

        let user = this.getUSerInfo();

        if (user === null)
            return false;
        else
            return true;

    }

    //Questo è un Intercettore (Interceptors).
    //Servono per svolgere un operazione prima che si invia una richiesta alle webapi
    setupAxiosInterceptors(token){

        console.log("Intercettore token Service: " + token);

        //Questo è il metodo per usare l'intercettore.
        axios.interceptors.request.use(
            //Creo una nuova configurazione
            (config) => {
                if(this.isLogged()){
                    config.headers.authorization = token
                }
                return config;
            },
			(err) => {
				return Promise.reject(err);
			}
        )

        //In saveUserInfo() dico all'intercettore quando dovrà essere utilizzato

    }

}

export default new AuthenticationService();