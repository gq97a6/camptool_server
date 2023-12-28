const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_';
const lookup = new Uint8Array(256);

for (let i = 0; i < chars.length; i++) {
    lookup[chars.charCodeAt(i)] = i;
}

const bufferToBase64 = function (arraybuffer) {
    const bytes = new Uint8Array(arraybuffer);

    let i;
    let len = bytes.length;
    let base64url = '';

    for (i = 0; i < len; i += 3) {
        base64url += chars[bytes[i] >> 2];
        base64url += chars[((bytes[i] & 3) << 4) | (bytes[i + 1] >> 4)];
        base64url += chars[((bytes[i + 1] & 15) << 2) | (bytes[i + 2] >> 6)];
        base64url += chars[bytes[i + 2] & 63];
    }

    if ((len % 3) === 2) {
        base64url = base64url.substring(0, base64url.length - 1);
    } else if (len % 3 === 1) {
        base64url = base64url.substring(0, base64url.length - 2);
    }

    return base64url;
}

const base64ToBuffer = function (base64string) {
    if (base64string) {

        let bufferLength = base64string.length * 0.75;

        let len = base64string.length;
        let i;
        let p = 0;

        let encoded1;
        let encoded2;
        let encoded3;
        let encoded4;

        let bytes = new Uint8Array(bufferLength);

        for (i = 0; i < len; i += 4) {
            encoded1 = lookup[base64string.charCodeAt(i)];
            encoded2 = lookup[base64string.charCodeAt(i + 1)];
            encoded3 = lookup[base64string.charCodeAt(i + 2)];
            encoded4 = lookup[base64string.charCodeAt(i + 3)];

            bytes[p++] = (encoded1 << 2) | (encoded2 >> 4);
            bytes[p++] = ((encoded2 & 15) << 4) | (encoded3 >> 2);
            bytes[p++] = ((encoded3 & 3) << 6) | (encoded4 & 63);
        }

        return bytes.buffer;
    }
}

function loginWebauthn() {
    var username = document.getElementById('username').value;
    var challenge = "";

    if (username == "") return

    fetch("/login/webauthn/begin", {
        method: "POST",
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        },
        body: JSON.stringify({
            username: username,
            displayName: username
        })
    })
    .then(res => {
        if (res.status === 200) return res;
        else throw new Error(res.statusText);
    })
    .then(res => res.json())
    .then(res => {
        challenge = res.challenge
        res.challenge = base64ToBuffer(res.challenge);
        if (res.allowCredentials) {
            for (let i = 0; i < res.allowCredentials.length; i++) {
            res.allowCredentials[i].id = base64ToBuffer(res.allowCredentials[i].id);
            }
        }
        return res;
    })
    .then(res => navigator.credentials.get({publicKey: res}))
    .then(credential => {
        return {
            challenge: challenge,
            username: username,
            id: credential.id,
            rawId: bufferToBase64(credential.rawId),
            response: {
                clientDataJSON: bufferToBase64(credential.response.clientDataJSON),
                authenticatorData: bufferToBase64(credential.response.authenticatorData),
                signature: bufferToBase64(credential.response.signature),
                userHandle: bufferToBase64(credential.response.userHandle),
            },
            type: credential.type
        };
    })
    .then(body => {
        return fetch("/login/webauthn/finish", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        })
    })
    .then(res => {
        if (res.status == 200) window.location.replace("/panel");
    });  
}

function registerWebauthn() {
    var username = getCookie("username");
    var challenge = "";

    fetch("/register/webauthn/begin", {
        method: "POST",
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        },
        body: JSON.stringify({
            username: username,
            displayName: username
        })
    })
    .then(res => {
        if (res.status === 200) return res;
        else throw new Error(res.statusText);
    })
    .then(res => res.json())
    .then(res => {
        challenge = res.challenge
        res.challenge = base64ToBuffer(res.challenge);
        res.user.id = base64ToBuffer(res.user.id);
        if (res.excludeCredentials) {
            for (let i = 0; i < res.excludeCredentials.length; i++) {
                res.excludeCredentials[i].id = base64ToBuffer(res.excludeCredentials[i].id);
            }
        }
          
        return res;
    })
    .then(res => navigator.credentials.create({publicKey: res}))
    .then(credential => {
        return {
            challenge: challenge,
            username: username,
            id: credential.id,
            rawId: bufferToBase64(credential.rawId),
            type: credential.type,
            response: {
                attestationObject: bufferToBase64(credential.response.attestationObject),
                clientDataJSON: bufferToBase64(credential.response.clientDataJSON)
            }
        };
    })
    .then(body => {
        return fetch("/register/webauthn/finish", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        })
    })
    .then(res => {
        if (res.status == 200) alert("register successful");
        else alert("register fail");
    });
}