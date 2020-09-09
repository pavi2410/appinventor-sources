<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!doctype html>
<html>
<body>
<!-- Source: https://gist.github.com/pavi2410/18195e3e6096aa257aa0341524d0da9e -->
<script>
    let registry = {} // map of type to class
    let mockInstances = {} // map of uuid to mockInstance

    class MockComponentRegistry {
        static register(type, mockClass) {
            registry[type] = mockClass
            console.log("<iframe>", "registering VCE", type, '<UUID = undefined>')
            postMessage('registerMockComponent', [], type, '') // TODO needs discussion
        }
    }

    window.addEventListener('message', event => {
        let {action, args, type, uuid} = JSON.parse(event.data)
        messageInterpreter(action, args, type, uuid)
    }, false)

    function messageInterpreter(action, args, type, uuid) {
        console.log("<iframe>", "got message", action, args, type, uuid)
        switch (action) {
            case "instantiateComponent": {
                let mockClass = registry[type]
                mockInstances[uuid] = new mockClass(uuid)
                break;
            }
            case "getName.callback": {
                console.log('getName.cb', type, uuid, args)
                break;
            }
            case "getPropertyValue.callback": {
                console.log('getPropVal.cb', type, uuid, args)
                break;
            }
            case 'onPropertyChange': {
                mockInstances[uuid].onPropertyChange(args[0], args[1])
                break;
            }
        }
    }

    function postMessage(action, args, type, uuid) {
        let msg = JSON.stringify({
            action,
            args,
            type,
            uuid
        })
        window.top.postMessage(msg, '*')
    }

    class MockVisibleExtension {
        constructor(type, uuid) {
            this.type = type
            this.uuid = uuid
        }

        initComponent(element) {
            this.$el = element
            postMessage('initializeComponent', [element.outerHTML], this.type, this.uuid)
        }

        getName() {
            postMessage('getName', [], this.type, this.uuid)
        }

        getPropertyValue(propName) {
            postMessage('getPropertyValue', [propName], this.type, this.uuid)
        }

        changeProperty(propName, newValue) {
            postMessage('changeProperty', [propName, newValue], this.type, this.uuid)
        }

        refresh(element) {
            element = element || this.$el
            postMessage('updateMockComponent', [element.outerHTML], this.type, this.uuid)
        }

        onPropertyChange() {
            this.refresh(this.$el);
        }
    }

    ${mockScriptFile}
</script>
</body>
</html>