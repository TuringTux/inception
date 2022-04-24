import { uuid } from '../../../anno-ui/utils'
import { fromTomlString } from '../utils/tomlString'
import { dispatchWindowEvent } from '../../../shared/util'
import SpanAnnotation from './span'
import RelationAnnotation from './relation.js'
import semver from 'semver'
import Ajv, { ValidateFunction } from 'ajv'
import AbstractAnnotation from './abstract'

/**
 * Annotation Container.
 */
export default class AnnotationContainer {

  set = new Set<AbstractAnnotation>()
  ajv = new Ajv({ allErrors: true })
  validate : ValidateFunction;

  /**
   * Constructor.
   */
  constructor() {
    this.validate = this.ajv.compile(require('../../../schemas/pdfanno-schema.json'))
  }

  /**
   * Add an annotation to the container.
   */
  add(annotation: AbstractAnnotation) {
    this.set.add(annotation)
    dispatchWindowEvent('annotationUpdated')
  }

  /**
   * Remove the annotation from the container.
   */
  remove(annotation: AbstractAnnotation) {
    this.set.delete(annotation)
    dispatchWindowEvent('annotationUpdated')
  }

  /**
   * Remove all annotations.
   */
  destroy() {
    console.log('AnnotationContainer#destroy')
    this.set.forEach(a => a.destroy())
    this.set = new Set()
  }

  /**
   * Get all annotations from the container.
   */
  getAllAnnotations() : AbstractAnnotation[] {
    let list = []
    this.set.forEach(a => list.push(a))
    return list
  }

  /**
   * Get annotations which user select.
   */
  getSelectedAnnotations() : AbstractAnnotation[] {
    return this.getAllAnnotations().filter(a => a.selected)
  }

  /**
   * Find an annotation by the id which an annotation has.
   */
  findById(uuid) {
    uuid = String(uuid) // `uuid` must be string.
    let annotation = null
    this.set.forEach(a => {
      if (a.uuid === uuid) {
        annotation = a
      }
    })
    return annotation
  }

  /**
   * Change the annotations color, if the text is the same in an annotation.
   *
   * annoType : span, one-way, two-way, link
   */
  changeColor({ text, color, uuid, annoType }) {
    console.log('changeColor: ', text, color, uuid)
    if (uuid) {
      const a = this.findById(uuid)
      if (a) {
        a.color = color
        a.render()
        a.enableViewMode()
      }
    } else {
      this.getAllAnnotations()
        .filter(a => a.text === text)
        .filter(a => {
          if (annoType === 'span') {
            return a.type === annoType
          } else if (annoType === 'relation') {
            if (a.type === 'relation' && a.direction === annoType) {
              return true
            }
          }
          return false
        }).forEach(a => {
          a.color = color
          a.render()
          a.enableViewMode()
        })
    }
  }

  setColor(colorMap) {
    console.log('setColor:', colorMap)
    Object.keys(colorMap).forEach(annoType => {
      if (annoType === 'default') {
        return
      }
      Object.keys(colorMap[annoType]).forEach(text => {
        const color = colorMap[annoType][text]
        this.changeColor({ text, color, annoType })
      })
    })
  }

  _findSpan(tomlObject, id) {
    return tomlObject.spans.find(v => {
      return id === v.id
    })
  }

  /**
   * Import annotations.
   */
  importAnnotations(data, isPrimary: boolean) {

    const readOnly = !isPrimary
    const colorMap = data.colorMap

    function getColor(index, type, text) {
      let color = colorMap.default
      if (colorMap[type] && colorMap[type][text]) {
        color = colorMap[type][text]
      }
      return color
    }

    return new Promise((resolve, reject) => {

      // Delete old ones.
      this.getAllAnnotations()
        .filter(a => a.readOnly === readOnly)
        .forEach(a => a.destroy())

      // Add annotations.
      data.annotations.forEach((tomlString, i) => {

        // Create a object from TOML string.
        let tomlObject = fromTomlString(tomlString)
        if (!tomlObject) {
          return
        }

        let pdfannoVersion = tomlObject.pdfanno || tomlObject.version

        if (semver.gt(pdfannoVersion, '0.4.0')) {
          // schema Validation
          if (!this.validate(tomlObject)) {
            reject(this.validate.errors)
            return
          }
          this.importAnnotations041(tomlObject, i, readOnly, getColor)
        } else {
          this.importAnnotations040(tomlObject, i, readOnly, getColor)
        }
      })

      // Done.
      resolve(true)
    })
  }

  /**
   * Import annotations.
   */
  importAnnotations040(tomlObject, tomlIndex, readOnly, getColor) {

    for (const key in tomlObject) {

      let d = tomlObject[key]

      // Skip if the content is not object, like version string.
      if (typeof d !== 'object') {
        continue
      }

      d.uuid = uuid()
      d.readOnly = readOnly

      if (d.type === 'span') {

        let span = SpanAnnotation.newInstanceFromTomlObject(d)
        span.color = getColor(tomlIndex, span.type, span.text)
        span.save()
        span.render()
        span.enableViewMode()

        // Relation.
      } else if (d.type === 'relation') {

        d.rel1 = tomlObject[d.ids[0]].uuid
        d.rel2 = tomlObject[d.ids[1]].uuid
        let relation = RelationAnnotation.newInstanceFromTomlObject(d)
        relation.color = getColor(tomlIndex, relation.direction, relation.text)
        relation.save()
        relation.render()
        relation.enableViewMode()

      } else {
        console.log('Unknown: ', key, d)
      }
    }
  }

  /**
   * Import annotations.
   */
  importAnnotations041(tomlObject, tomlIndex, readOnly, getColor) {

    // order is important.
    ;['spans', 'relations'].forEach(key => {
      const objs = tomlObject[key]
      if (Array.isArray(objs)) {
        objs.forEach(obj => {
          obj.uuid = obj.id || uuid()
          obj.readOnly = readOnly

          if (key === 'spans') {
            const span = SpanAnnotation.newInstanceFromTomlObject(obj)
            span.save()
            span.render()
            span.enableViewMode()

          } else if (key === 'relations') {
            const span1 = this._findSpan(tomlObject, obj.head)
            const span2 = this._findSpan(tomlObject, obj.tail)
            obj.rel1 = span1 ? span1.uuid : null
            obj.rel2 = span2 ? span2.uuid : null
            const relation = RelationAnnotation.newInstanceFromTomlObject(obj)
            relation.save()
            relation.render()
            relation.enableViewMode()
          }
        })
      }
    })
  }
}
