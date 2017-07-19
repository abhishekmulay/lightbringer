/**
 * Created by abhishek on 7/15/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';
import {updateItem, selectItem} from  '../actions/index';
import {bindActionCreators} from 'redux';
const request = require('superagent');

class EvaluationBar extends Component {
  constructor(props) {
    super(props);
    var evalDone = this.props && this.props.item && this.props.item._source.evaluation_done;
    this.state = {showEvaluationButtons: !evalDone};
    this.evaluate = this.evaluate.bind(this);
  }

  render() {

    if (!this.props.item) {
      return <div></div>
    }

    return (
        <div id="EvaluationBar">
          {/*<div className={this.props.item._source.evaluation_done ? 'hidden' : 'row' }>*/}
          <div className='row'>
            <div className="col-xs-4">
              <button className="btn btn-danger btn-md" value={0} onClick={(e) => this.evaluate(e)}>Score: 0</button>
            </div>
            <div className="col-xs-4">
              <button className="btn btn-default btn-md" value={1} onClick={(e) => this.evaluate(e)}>Score: 1</button>
            </div>
            <div className="col-xs-4">
              <button className="btn btn-success btn-md" value={2} onClick={(e) => this.evaluate(e)}>Score: 2</button>
            </div>
          </div>

          {/*<div className={this.props.item._source.evaluation_done ? 'row' : 'hidden'}>*/}
            {/*<div className="col-xs-12">*/}
              {/*<p>*/}
                {/*This document has been evaluated by*/}
                {/*<strong>{this.props.item._source.evaluator}</strong> and has score*/}
                {/*<strong>{this.props.item._source.score}</strong>*/}
                {/*<button className="btn btn-default btn-md" onClick={() => {this.props.item._source.evaluation_done = false}}>Re-evaluate?</button>*/}
              {/*</p>*/}
            {/*</div>*/}
          {/*</div>*/}
        </div>
    )
  }

  evaluate(event) {
    const self = this;
    const id = this.props.item._id || '';
    const score = event.target.value || -1;
    const evaluator = 'Abhishek';
    const evaluationEndpoint = "http://localhost:4000/evaluate";
    request
        .post(evaluationEndpoint)
        .send({'id': id, 'score': score, 'evaluator': evaluator}) // sends a JSON post body
        .set('Accept', 'application/json')
        .end(function (err, res) {
          // Calling the end function will send the request
          console.log(res);
          let updatedItem = self.props.item;
          updatedItem['_source']['score'] = score;
          updatedItem['_source']['evaluator'] = evaluator;
          updatedItem['_source']['evaluation_done'] = true;
          self.dispatchItemUpdate(updatedItem);
        });
  }

  dispatchItemUpdate(updatedItem) {
    this.props.updateItem(updatedItem);
    this.props.selectItem(updatedItem);
  }
}

// receive active item from redux
function mapStateToProps(state) {
  return {
    item: state.activeItem
  }
}

// send action with updated item
function mapDispatchToProps(dispatch) {
  return bindActionCreators({updateItem: updateItem, selectItem: selectItem}, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(EvaluationBar);