package tsar.alex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tsar.alex.dto.InitializeUserRatingRequest;
import tsar.alex.dto.StartMatchRequest;
import tsar.alex.model.Pair;
import tsar.alex.model.UserRating;
import tsar.alex.model.UserWaitingForMatch;

@Mapper(componentModel = "spring")
public interface MatcherMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "rating", expression = "java(tsar.alex.utils.Constants.getInitialUserRating())")
    UserRating mapToUserRating(InitializeUserRatingRequest request);

    @Mapping(target = "pairOfUsersIds", expression = "java(new tsar.alex.model.Pair(user0.getUserRating().getUserId(), user1.getUserRating().getUserId()))")
    StartMatchRequest mapToStartMatchRequest(UserWaitingForMatch user0, UserWaitingForMatch user1);

    default Pair<UserWaitingForMatch> mapToUsersPair(UserWaitingForMatch user0, UserWaitingForMatch user1) {
        return new Pair<>(user0, user1);
    }

}