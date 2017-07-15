/**
 * Created by abhishek on 7/15/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';
import {selectItem} from  '../actions/index';
import {bindActionCreators} from 'redux';
const request = require('superagent');

class EvaluationBar extends Component {
  constructor(props) {
    super(props);
    this.evaluate = this.evaluate.bind(this);
  }

  render() {

    if (!this.props.item) {
      return <div></div>
    }

    return (
        <div id="EvaluationBar">
          <div className={this.props.item._source.evaluation_done ? 'hidden' : 'row'}>
            <div className="col-xs-4">
              <button className="btn btn-default btn-md" value={0}>Score: 0</button>
            </div>
            <div className="col-xs-4">
              <button className="btn btn-warning btn-md" value={1}>Score: 1</button>
            </div>
            <div className="col-xs-4">
              <button className="btn btn-success btn-md" value={2} onClick={(e) => this.evaluate(e)}>Score: 2</button>
            </div>
          </div>
          <div className={this.props.item._source.evaluation_done ? 'row' : 'hidden'} >
            <div className="col-xs-12">
              <p className="background-danger">
                This document has been evaluated by
                <strong>{this.props.item._source.evaluator}</strong> and has score
                <strong>{this.props.item._source.score}</strong>
              </p>
            </div>
          </div>
        </div>
    )
  }

  evaluate(event) {
    const id = this.props.item._id || '';
    const score = event.target.value || -1;
    const evaluationEndpoint = "http://localhost:4000/evaluate";
    request
        .post(evaluationEndpoint)
        .send({'id': id, 'score': score, 'evaluator': 'Abhishek'}) // sends a JSON post body
        .set('Accept', 'application/json')
        .end(function (err, res) {
          // Calling the end function will send the request
          console.log(res);
        });
  }
}


function mapStateToProps(state) {
  return {
    item: state.activeItem
  }
}

export default connect(mapStateToProps)(EvaluationBar);