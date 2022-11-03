@txn
Feature: Upload, download and delete resources

  Resource service allows user to perform CRD operations with mp3 files

  Scenario: Upload new resource
    When User uploads file "file_example_2MB.mp3"
    Then Application responds with status 200
    And Upload response is
      """
      {"id": 1}
      """
    And The following resources are stored in the system
      | fileName                 | id | size    |
      | file_example_2MB.mp3     | 1  | 2118050 |

  Scenario: Download existing resource
    Given The following resources exist in the system
      | fileName                 | id | size    |
      | file_example_1MB.mp3     | 2  | 1087849 |
    When User downloads resource with id=2
    Then Application responds with status 200
    And Response content type is "audio/mpeg"
    And Response contains file with size 1087849

  Scenario: Delete existing resource
    Given The following resources exist in the system
      | fileName                 | id |
      | file_example_2MB.mp3     | 3  |
    When User deletes resource with id=3
    Then Application responds with status 200
    And Delete response is
      """
      {"ids": [3]}
      """
