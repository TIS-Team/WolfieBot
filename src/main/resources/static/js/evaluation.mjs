import {API_URL} from "/js/environment.mjs";

document.addEventListener("DOMContentLoaded", function () {
    getEvaluationData(getEvaluationIdFromUrl()).then(response => {
        console.log(response);
    });
});

function getEvaluationIdFromUrl() {
    const pathName = window.location.pathname;
    return pathName.split("/").pop();
}

async function getEvaluationData(evaluationId) {
    const url = API_URL + `/evaluation/${evaluationId}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Response status: ${response.status}`);
        }

        const json = await response.json();
        console.log(json);
        return json;
    } catch (error) {
        console.error(error.message);
        return null;
    }
}
