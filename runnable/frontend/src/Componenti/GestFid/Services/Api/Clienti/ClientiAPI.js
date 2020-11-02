import axios from "axios";

//Metodi per comunicare con le API backend relative ai clienti.
class ClientiService {

    state = {
        Server: "http://localhost:8080",
        BaseURL: "/api/clienti"
    }

    getAllClientiData = () => {
        return axios.get(`${this.state.Server}${this.state.BaseURL}/cerca/all`);
    }

    getClientiByCode = (codfid) => {
        return axios.get(`${this.state.Server}${this.state.BaseURL}/cerca/codice/${codfid}`);
    }

    delClienteByCode = (codfid) => {
        return axios.delete(`${this.state.Server}${this.state.BaseURL}/elimina/codfid/${codfid}`);
    }

    insCliente = (cliente) => {
        return axios.post(`${this.state.Server}${this.state.BaseURL}/inserisci`, cliente);
    }

}

export default new ClientiService();