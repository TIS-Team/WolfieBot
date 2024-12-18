import {API_URL} from './environment.mjs';

export async function getEvaluationData(evaluationId) {
    const url = API_URL + `/evaluation/${evaluationId}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Response status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(error.message);
        return null;
    }
}

export async function getRanksData() {
    const url = API_URL + `/ranks`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Error fetching ranks: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(error.message);
        return null;
    }

}

export async function getActionsData() {
    const url = API_URL + `/actions`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Error fetching actions: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(error.message);
        return null;
    }
}


/**
 * Sends evaluation data to the server for submission.
 *
 * This function retrieves the evaluation ID from the URL and constructs the
 * submission endpoint URL. It sends the provided data as a JSON payload to the
 * server using a POST request. If the request is successful, it handles the
 * success case (e.g., showing a notification or redirecting). If the request
 * fails, it logs the error.
 *
 * @param {Object} data - The evaluation data to be submitted.
 */


export async function sendEvaluationData(data) {
    const evaluationId = getEvaluationIdFromUrl();
    const url = `${API_URL}/evaluation/${evaluationId}/submit`;
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            throw new Error(`Failed to send data: ${response.status}`);
        }
    } catch (error) {
        console.error(error);
    }
}