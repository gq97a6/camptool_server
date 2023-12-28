function login() {
    // Get the username and password from the form
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Create the payload
    const payload = {
        username: username,
        password: password
    };

    // Make the POST request
    fetch('/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (response.status === 200) window.location.href = '/panel';
    });
}