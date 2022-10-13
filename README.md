# Mobserve

If you ever wanted a tool to simply push the calls and SMS (or text messages) from your phone to somewhere remote, this is it.
This app matches all missed (in case of calls), incoming and/or outgoing calls text messages against set rules and sends them over to webhook that you define.

[![Build](https://github.com/syncloudsoftech/mobserve/actions/workflows/debug.yml/badge.svg)](https://github.com/syncloudsoftech/mobserve/actions/workflows/debug.yml)
[![Release](https://badgen.net/github/release/syncloudsoftech/mobserve)](https://github.com/syncloudsoftech/mobserve/releases)
[![Downloads](https://badgen.net/github/assets-dl/syncloudsoftech/mobserve)](https://github.com/syncloudsoftech/mobserve/releases/latest)
[![License](https://badgen.net/github/license/syncloudsoftech/mobserve)](https://github.com/syncloudsoftech/mobserve/blob/main/LICENSE)

If you find this app useful, send over your hugs :hugs: to [Syncloud Softech](https://syncloudsoft.com/).

### Usage

The app sends tiny payload similar to what's shown below on the remote webhook (as [JSON](https://www.json.org/) body in a `POST` request):

```json
{
    "event": "call",
    "direction": "incoming",
    "participant": "+919876543210",
    "duration": 143,
    "date": 1609459200000
}
```

```json
{
    "event": "sms",
    "direction": "incoming",
    "participant": "+919876543210",
    "content": "Your OTP for login is 123456.",
    "date": 1609459200000
}
```

You can use services like [Pipedream](https://pipedream.com/) to process these payloads and do stuff e.g., sending over to a Slack channel using below code:

```js
import { axios } from "@pipedream/platform"

const colors = {
  'incoming': '#00aeff',
  'outgoing': '#7ac143',
  'missed': '#e4002b',
}

const events = {
  'call': 'phone call',
  'sms': 'text message',
}

export default defineComponent({
  async run({ $, event }) {
    const content = event.body.event === "call"
      ? `Call duration was ${event.body.duration} seconds.`
      : (event.body.content || 'No message body was specified.');
    return await axios($, {
      url: 'https://hooks.slack.com/services/<webhook>',
      method: 'post',
      data: {
        text: `New ${event.body.direction} ${events[event.body.event]} captured.`,
        attachments: [{
          author_name: event.body.participant,
          color: colors[event.body.direction],
          footer: 'Mobserve',
          text: content,
          ts: event.body.date / 1000,
        }],
      },
    })
  },
})
```

### Download

You can download the latest version from [Releases](https://github.com/syncloudsoftech/mobserve/releases) page.

### License

See [LICENSE](LICENSE) file.
