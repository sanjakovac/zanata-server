jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import LanguagesDropdown from '../../app/components/LanguagesDropdown'
import Dropdown from '../../app/components/Dropdown'
import Icon from '../../app/components/Icon'

describe('LanguageDropdownTest', () => {
  it('LanguagesDropdown markup', () => {
    const awayEreBreakOfDay = () => {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <LanguagesDropdown
        context={{
          projectVersion: {
            project: {
              slug: 'middle'
            },
            version: 'earth',
            locales: {
              wes: {
                id: 'wes',
                name: 'Westron'
              },
              roh: {
                id: 'roh',
                name: 'Rohirric'
              },
              khu: {
                id: 'khu',
                name: 'Khuzdul'
              },
              val: {
                id: 'val',
                name: 'Valarin'
              }
            }
          },
          selectedDoc: {
            id: 'misty-mountains.txt'
          },
          selectedLocale: 'khu'
        }}
        toggleDropdown={awayEreBreakOfDay}
        isOpen={true}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <Dropdown onToggle={awayEreBreakOfDay}
                isOpen={true}>
        <Dropdown.Button>
          <button className="Link--invert">
            Khuzdul
            <Icon name="chevron-down"
                  className="Icon--sm Dropdown-toggleIcon u-sML-1-8"/>
          </button>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li key="wes">
              <a href="#/middle/earth/translate/misty-mountains.txt/wes"
                 className="Dropdown-item">
                Westron
              </a>
            </li>
            <li key="roh">
              <a href="#/middle/earth/translate/misty-mountains.txt/roh"
                 className="Dropdown-item">
                Rohirric
              </a>
            </li>
            <li key="khu">
              <a href="#/middle/earth/translate/misty-mountains.txt/khu"
                 className="Dropdown-item">
                Khuzdul
              </a>
            </li>
            <li key="val">
              <a href="#/middle/earth/translate/misty-mountains.txt/val"
                 className="Dropdown-item">
                Valarin
              </a>
            </li>
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
    expect(actual).toEqual(expected)
  })
})
