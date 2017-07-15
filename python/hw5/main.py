from flask import Flask, Response, json, request
from flask_cors import CORS, cross_origin

import properties
from hw5.elastic_server import ElasticSearchServer

es_server = ElasticSearchServer(properties.team_index, properties.team_type)

app = Flask(__name__)
CORS(app)
#####################################################

@app.route('/')
def hello_world():
    return 'Hello, World!'

@app.route('/search', methods=['POST', 'OPTIONS'])
@cross_origin()
def search_handler():
    request_params = request.get_json()
    search_term = request_params.get('search_term', '')
    scroll_id = request_params.get('scroll_id', '')
    result = es_server.search(search_term, scroll_id)
    return send_json(result, 200)

@app.route('/evaluate', methods=['POST', 'OPTIONS'])
@cross_origin()
def evaluation_handler():
    request_params = request.get_json()
    id = request_params.get('id', '')
    score = request_params.get('score', -1)
    evaluator = request_params.get('evaluator', '')
    result = es_server.evaluate(id, score, evaluator)
    return send_json(result, 200)


#####################################################
def send_json(data, code, headers=None):
    """Makes a Flask response with a JSON encoded body"""
    resp = Response(json.dumps(data), code, mimetype='application/json')
    resp.headers.extend(headers or {})
    return resp

if __name__ == '__main__':
    app.debug = True
    app.run(port=4000)
