const formularioEspecie = document.querySelector("#formulario-especie");
const resultadoEspecie = document.querySelector("#resultado-especie");

if (formularioEspecie && resultadoEspecie) {
    formularioEspecie.addEventListener("submit", (event) => {
        event.preventDefault();

        const datos = Object.fromEntries(new FormData(formularioEspecie).entries());
        console.log("Datos recibidos del formulario de especie:", datos);
        resultadoEspecie.textContent = JSON.stringify(datos, null, 2);
        resultadoEspecie.hidden = false;
        
    });
}