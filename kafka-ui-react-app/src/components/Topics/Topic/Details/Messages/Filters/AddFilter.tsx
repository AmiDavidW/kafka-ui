import React from 'react';
import * as S from 'components/Topics/Topic/Details/Messages/Filters/Filters.styled';
import { MessageFilters } from 'components/Topics/Topic/Details/Messages/Filters/Filters';
import { FilterEdit } from 'components/Topics/Topic/Details/Messages/Filters/FilterModal';
import SavedFilters from 'components/Topics/Topic/Details/Messages/Filters/SavedFilters';
import SavedIcon from 'components/common/Icons/SavedIcon';

import AddEditFilterContainer from './AddEditFilterContainer';

export interface FilterModalProps {
  toggleIsOpen(): void;
  filters: MessageFilters[];
  addFilter(values: MessageFilters): void;
  deleteFilter(index: number): void;
  activeFilterHandler(activeFilter: MessageFilters, index: number): void;
  toggleEditModal(): void;
  editFilter(value: FilterEdit): void;
}

const AddFilter: React.FC<FilterModalProps> = ({
  toggleIsOpen,
  filters,
  addFilter,
  deleteFilter,
  activeFilterHandler,
  toggleEditModal,
  editFilter,
}) => {
  const [savedFilterState, setSavedFilterState] =
    React.useState<boolean>(false);

  const onSubmit = React.useCallback(
    async (values: MessageFilters, saveFilter) => {
      if (saveFilter) {
        addFilter(values);
      } else {
        activeFilterHandler(values, -1);
      }
    },
    [activeFilterHandler, addFilter]
  );
  return (
    <>
      <S.FilterTitle>Add filter</S.FilterTitle>
      {savedFilterState ? (
        <SavedFilters
          deleteFilter={deleteFilter}
          activeFilterHandler={activeFilterHandler}
          onCancelBtn={toggleIsOpen}
          onGoBack={() => setSavedFilterState(false)}
          filters={filters}
          onEdit={(index, filter) => {
            toggleEditModal();
            editFilter({ index, filter });
          }}
        />
      ) : (
        <>
          <S.SavedFiltersTextContainer
            onClick={() => setSavedFilterState(true)}
          >
            <SavedIcon /> <S.SavedFiltersText>Saved Filters</S.SavedFiltersText>
          </S.SavedFiltersTextContainer>
          <AddEditFilterContainer
            cancelBtnHandler={toggleIsOpen}
            submitBtnText="Add filter"
            submitCallback={onSubmit}
            createNewFilterText="Create a new filter"
          />
        </>
      )}
    </>
  );
};

export default AddFilter;
